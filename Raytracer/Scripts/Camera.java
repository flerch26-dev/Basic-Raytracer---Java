import java.io.FileWriter;
import java.io.IOException;

public class Camera extends Utilities
{
    static FileWriter writer;

    public float aspectRatio = 16.0f / 9.0f;
    public int imageWidth = 500;
    public int samplesPerPixel = 50;
    public int max_depth = 50;

    public float vfov = 20;
    public Vector3 lookFrom = new Vector3(13,2,3);
    public Vector3 lookAt = new Vector3(0, 0, 0);
    public Vector3 vup = new Vector3(0, 1, 0);
    Vector3 u, v, w;

    public float defocusAngle = 0.6f;
    public float focusDst = 10.0f;
    Vector3 defocusDiscU;
    Vector3 defocusDiscV;

    Vector3 background = new Vector3(0.70f, 0.80f, 1.00f);

    public void Render(Hittable[] objects) throws IOException
    {
        CreateImageFile();
        writer = new FileWriter("Image.ppm");
        writer.write("P3\n" + imageWidth + " " + imageHeight + "\n255\n");

        long startTime = System.nanoTime();
        for (int j = 0; j < imageHeight; j++)
        {
            System.out.print("\033[H\033[2J");  
            System.out.flush();  
            System.out.println("Scanlines remaining : " + (imageHeight - j));
            for (int i = 0; i < imageWidth; i++)
            {
                Vector3 pixelColor = new Vector3(0, 0, 0);

                for (int sample = 0; sample < samplesPerPixel; sample++)
                {
                    Ray r = GetRay(i,j);
                    pixelColor = pixelColor.add(RayColor(r, max_depth, objects));

                }
                WriteColor(writer, pixelColor.mult(pixelSamplesScale));
            }
        }
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);

        System.out.println("Done. Render took " + (duration / 1000000000f) + " s");
        writer.close();
    }

    static int imageHeight;
    static Vector3 center;
    static Vector3 pixel00loc;
    static Vector3 pixelDeltaU;
    static Vector3 pixelDeltaV;
    static float pixelSamplesScale;

    void Initialize()
    {
       imageHeight = (int)(imageWidth / aspectRatio) < 1 ? 1 : (int)(imageWidth / aspectRatio);

        //Camera
        float theta = degreeToRadians(vfov);
        float h = (float)Math.tan(theta / 2);
        float viewportHeight = 2 * h * focusDst;
        float viewportWidth = viewportHeight * ((float)imageWidth/imageHeight);
        pixelSamplesScale = 1.0f / samplesPerPixel;
        center = lookFrom;

        w = lookFrom.sub(lookAt).normalize();
        u = cross(vup, w).normalize();
        v = cross(w, u);

        // Calculate the vectors across the horizontal and down the vertical viewport edges
        Vector3 viewportU = u.mult(viewportWidth);
        Vector3 viewportV = v.mult(-viewportHeight);

        // Calculate the horizontal and vertical delta vectors from pixel to pixel
        pixelDeltaU = viewportU.div(imageWidth);
        pixelDeltaV = viewportV.div(imageHeight);

        // Calculate the location of the upper left pixel
        Vector3 viewPortUpperLeft = center.sub(w.mult(focusDst)).sub(viewportU.div(2)).sub(viewportV.div(2));
        pixel00loc = viewPortUpperLeft.add(pixelDeltaU.add(pixelDeltaV).mult(0.5f));

        float defocusRadius = focusDst * (float)Math.tan(degreeToRadians(defocusAngle / 2));
        defocusDiscU = u.mult(defocusRadius);
        defocusDiscV = v.mult(defocusRadius);
    }

    Ray GetRay(int i, int j)
    {
        Vector3 offset = SampleSquare();
        Vector3 pixelSample = pixel00loc.add(pixelDeltaU.mult(i + offset.x)).add(pixelDeltaV.mult(j + offset.y));
        Vector3 rayOrigin = defocusAngle <= 0 ? center : DefocusDiskSample();
        Vector3 rayDirection = pixelSample.sub(rayOrigin);
        return new Ray(rayOrigin, rayDirection);
    }

    Vector3 DefocusDiskSample()
    {
        Vector3 p = RandomInUnitDisc();
        return center.add(defocusDiscU.mult(p.x)).add(defocusDiscV.mult(p.y));
    }

    Vector3 SampleSquare()
    {
        return new Vector3((float)Math.random() - 0.5f, (float)Math.random() - 0.5f, 0);
    }

    Vector3 RayColor(Ray r, int depth, Hittable[] objects)
    {
        if (depth <= 0) return new Vector3(0,0,0);

        HitRecord hitInfo = new HitRecord();
        hitInfo = hit(r, new Interval(0.001f, 1000000000f), objects);
        if (hitInfo.normal == null)
        {
            return background;
        }

        Vector3 colorFromEmission = hitInfo.mat.emitted(hitInfo.u, hitInfo.v, hitInfo.p);
        MaterialScatter scatterInfo = hitInfo.mat.scatter(r, hitInfo);

        if (scatterInfo == null) return colorFromEmission;
        Vector3 colorFromScatter = scatterInfo.attenuation.mult(RayColor(scatterInfo.scattered, depth - 1, objects));
        return colorFromEmission.add(colorFromScatter);
    }

    public static HitRecord hit(Ray r, Interval ray_t, Hittable[] objects)
    {
        HitRecord rec = new HitRecord();
        float closestSoFar = ray_t.max;

        for(Hittable obj : objects)
        {
            HitRecord tempRec = obj.hit(r, ray_t);
            //System.out.println(tempRec.t);
            if (tempRec != null)
            {
                if (tempRec.t < closestSoFar)
                {
                    rec = tempRec;
                    closestSoFar = rec.t;
                }
            }
        }
        return rec;
    }
}
