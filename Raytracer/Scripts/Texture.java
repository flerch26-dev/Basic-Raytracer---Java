import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public abstract class Texture extends Utilities
{
    public abstract Vector3 Value(float u, float v, Vector3 p);
}

class SolidColor extends Texture
{
    Vector3 albedo;

    public SolidColor(Vector3 albedo)
    {
        this.albedo = albedo;
    }

    public SolidColor(float r, float g, float b)
    {
        this.albedo = new Vector3(r, g, b);
    }

    public Vector3 Value(float u, float v, Vector3 p)
    {
        return albedo;
    }
}

class CheckerTexture extends Texture
{
    float invScale;
    Texture even;
    Texture odd;

    public CheckerTexture(float scale, Texture even, Texture odd)
    {
        this.invScale = 1.0f / scale;
        this.even = even;
        this.odd = odd;
    }

    public CheckerTexture(float scale, Vector3 even, Vector3 odd)
    {
        this.invScale = 1.0f / scale;
        this.even = new SolidColor(even);
        this.odd = new SolidColor(odd);
    }

    public Vector3 Value(float u, float v, Vector3 p)
    {
        int xInteger = (int)Math.floor(invScale * p.x);
        int yInteger = (int)Math.floor(invScale * p.y);
        int zInteger = (int)Math.floor(invScale * p.z);

        boolean isEven = (xInteger + yInteger + zInteger) % 2 == 0;
        return isEven ? even.Value(u, v, p) : odd.Value(u, v, p);
    }
}

class ImageTexture extends Texture
{
    BufferedImage img;

    public ImageTexture(String filename) throws IOException
    {
        LoadImage(filename);
    }

    public Vector3 Value(float u, float v, Vector3 p)
    {
        if (img.getHeight() <= 0) return new Vector3(0, 1, 1);
        u = new Interval(0,1).clamp(u);
        v = 1 - new Interval(0,1).clamp(v);

        int i = (int)(u * img.getWidth());
        int j = (int)(v * img.getHeight());
        Color pixelCol = new Color(img.getRGB(i, j));
        Vector3 pixel = new Vector3(pixelCol.getRed(), pixelCol.getGreen(), pixelCol.getBlue());

        float colorScale = 1.0f / 255.0f;
        return pixel.mult(colorScale);
    }

    void LoadImage(String filename)
    {
        try {
            img = ImageIO.read(new File(filename));
        } catch (IOException e) {
            System.out.println("Image couldn't be loaded");
        }
    }
}
