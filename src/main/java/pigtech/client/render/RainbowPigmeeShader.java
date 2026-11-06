/*
 * Pigmee Tech - RainbowPigmeeShader (1.12.2)
 * Port of ae2lt 1.21.1 assets/ae2lt/shaders/core/rainbow_pigmee.{vsh,fsh}.
 *
 * 彩虹猪咪的表面色带 shader。1.12.2 没有核心着色器管线，
 * 改用原始 GL20 编译/链接，GLSL 120 兼容语法重写：
 *   in/out → varying，gl_FragColor 输出，固定管线内置变量提供
 *   gl_Vertex / gl_Color / gl_MultiTexCoord0-1 / gl_ModelViewProjectionMatrix。
 * 顶点数据通过 Tessellator 的自定义 VertexFormat（POSITION+COLOR+TEX+TEX）传入，
 * 两套 TEX_2F 分别映射到纹理单元 0/1 的 gl_MultiTexCoord0/1。
 * 不支持 shader 的环境降级为整体变色 tint（RainbowPigmeeColors）。
 */
package pigtech.client.render;

import pigtech.PigmeeTech;
import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public final class RainbowPigmeeShader {

    /** 逐字对齐源码 rainbow_pigmee.vsh 的数据流（仅语法换为 GLSL 120） */
    private static final String VERTEX_SRC =
            "#version 120\n"
            + "varying vec2 surfaceUV;\n"
            + "varying vec4 surfaceShade;\n"
            + "varying vec2 modelDepthHeight;\n"
            + "void main() {\n"
            + "    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;\n"
            + "    surfaceUV = gl_MultiTexCoord0.xy;\n"
            + "    surfaceShade = gl_Color;\n"
            + "    modelDepthHeight = gl_MultiTexCoord1.xy / 4096.0;\n"
            + "}\n";

    /** 逐字对齐源码 rainbow_pigmee.fsh 的色带算法（仅语法换为 GLSL 120） */
    private static final String FRAGMENT_SRC =
            "#version 120\n"
            + "uniform float AnimationTicks;\n"
            + "varying vec2 surfaceUV;\n"
            + "varying vec4 surfaceShade;\n"
            + "varying vec2 modelDepthHeight;\n"
            + "void main() {\n"
            // 量化空间而非时间：像素块干脆利落，彩虹平滑流动
            + "    vec2 cell = floor(surfaceUV * 16.0);\n"
            // 深度主导，高度偏移让色带从前向后/向上倾斜
            + "    vec2 flowCell = floor(modelDepthHeight * 16.0);\n"
            + "    float phase = fract((dot(flowCell + 0.5, vec2(8.0, 3.0)) - AnimationTicks) / 160.0);\n"
            + "    vec3 hue = clamp(abs(fract(phase + vec3(0.0, 2.0 / 3.0, 1.0 / 3.0)) * 6.0 - 3.0) - 1.0, 0.0, 1.0);\n"
            + "    vec3 rainbow = mix(vec3(1.0), hue, 0.72);\n"
            // 固定的有序颗粒避免高光质感或动态噪点
            + "    float grain = mod(cell.x + cell.y * 2.0, 4.0);\n"
            + "    float textureShade = 0.94 + grain * 0.02;\n"
            + "    gl_FragColor = vec4(rainbow * textureShade * surfaceShade.rgb, surfaceShade.a);\n"
            + "}\n";

    /** -1 = 未初始化，0 = 不可用（不支持或编译失败），>0 = 程序句柄 */
    private static int program = -1;
    private static int animationTicksUniform = -1;

    private RainbowPigmeeShader() {
    }

    public static boolean isAvailable() {
        if (program == -1) {
            init();
        }
        return program > 0;
    }

    public static void bind() {
        GL20.glUseProgram(program);
    }

    public static void unbind() {
        GL20.glUseProgram(0);
    }

    public static void setAnimationTicks(float ticks) {
        GL20.glUniform1f(animationTicksUniform, ticks);
    }

    private static void init() {
        if (!OpenGlHelper.shadersSupported) {
            PigmeeTech.LOGGER.info("Shaders not supported; rainbow pigmee falls back to flat tint");
            program = 0;
            return;
        }
        try {
            int vertex = compile(GL20.GL_VERTEX_SHADER, VERTEX_SRC);
            int fragment = compile(GL20.GL_FRAGMENT_SHADER, FRAGMENT_SRC);
            if (vertex == 0 || fragment == 0) {
                program = 0;
                return;
            }
            int p = GL20.glCreateProgram();
            GL20.glAttachShader(p, vertex);
            GL20.glAttachShader(p, fragment);
            GL20.glLinkProgram(p);
            if (GL20.glGetProgrami(p, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
                PigmeeTech.LOGGER.warn("Rainbow pigmee shader link failed: {}",
                        GL20.glGetProgramInfoLog(p, 1024));
                GL20.glDeleteProgram(p);
                program = 0;
                return;
            }
            GL20.glDetachShader(p, vertex);
            GL20.glDetachShader(p, fragment);
            GL20.glDeleteShader(vertex);
            GL20.glDeleteShader(fragment);
            animationTicksUniform = GL20.glGetUniformLocation(p, "AnimationTicks");
            program = p;
            PigmeeTech.LOGGER.info("Rainbow pigmee shader compiled");
        } catch (Throwable t) {
            PigmeeTech.LOGGER.warn("Rainbow pigmee shader unavailable", t);
            program = 0;
        }
    }

    private static int compile(int type, String source) {
        int shader = GL20.glCreateShader(type);
        GL20.glShaderSource(shader, source);
        GL20.glCompileShader(shader);
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            PigmeeTech.LOGGER.warn("Rainbow pigmee shader compile failed: {}",
                    GL20.glGetShaderInfoLog(shader, 1024));
            GL20.glDeleteShader(shader);
            return 0;
        }
        return shader;
    }
}
