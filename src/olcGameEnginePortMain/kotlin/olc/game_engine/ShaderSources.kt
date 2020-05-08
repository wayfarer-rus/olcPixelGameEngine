package olc.game_engine

// language=glsl
const val layerVertexShaderSource = """
#version 330 core
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec4 aColor;
layout (location = 2) in vec2 aTexCoord;

out vec4 ourColor;
out vec2 TexCoord;

void main()
{
    gl_Position = vec4(aPos, 1.0);
    ourColor = aColor;
    TexCoord = aTexCoord;
}
"""

const val layerFragmentShaderSource = """
#version 330 core
out vec4 FragColor;
  
in vec4 ourColor;
in vec2 TexCoord;

uniform sampler2D ourTexture;

void main()
{
    FragColor = texture(ourTexture, TexCoord)*ourColor;
}
"""

// language=glsl
const val textureVertexShaderSource = """
#version 330 core
layout (location = 0) in vec2 aPos;
layout (location = 1) in vec4 aColor;
layout (location = 2) in vec4 aTexCoord;

out vec4 ourColor;
out vec4 TexCoord;

void main()
{
    gl_Position = vec4(aPos, 0.0, 1.0);
    ourColor = aColor;
    TexCoord = aTexCoord;
}
"""

// language=glsl
const val textureFragmentShaderSource = """
#version 330 core
out vec4 FragColor;
  
in vec4 ourColor;
in vec4 TexCoord;

uniform sampler2D ourTexture;

void main()
{
    FragColor = texture(ourTexture, TexCoord.xy) * ourColor;
}  
"""