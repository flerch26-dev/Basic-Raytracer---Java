import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main extends Utilities
{
    static List<Hittable> world = new ArrayList<Hittable>();

    static Camera cam = new Camera();
    //cam.aspectRatio = 16.0f / 9.0f;
    //cam.imageWidth = 400f;

    public static void main(String[] args) throws IOException 
    {   
        CornellBox();
        cam.Initialize();
        cam.Render(world.toArray(new Hittable[world.size()]));
    }

    static void Monkey() throws IOException
    {
        Lambertian red = new Lambertian(new Vector3(.65f, .05f, .05f));
        Object monkey = new Object("/Users/florianlerch/Raytracer/Models/monkey.obj", red, new Vector3(0, 0, 200), new Vector3(0, (float)Math.PI, (float)Math.PI / 2), new Vector3(100, 100, 100));
        //Object cube2 = new Object("/Users/florianlerch/Raytracer/Models/GrassCube.obj", red);
        world.addAll(monkey.triangles);
        //world.addAll(cube2.triangles);

        DiffuseLight difflight = new DiffuseLight(new Vector3(6,6,6));
        world.add(new Sphere(new Vector3(-2, 0, 0), 2, difflight));

        cam.aspectRatio      = 1.0f;
        cam.imageWidth       = 400;
        cam.samplesPerPixel = 100;
        cam.max_depth         = 50;
        //cam.background        = new Vector3(255,255,255);

        cam.vfov     = 40;
        cam.lookFrom = new Vector3(0, 0, 15);
        cam.lookAt   = new Vector3(0, 0, 0);
        cam.vup      = new Vector3(0,1,0);

        cam.defocusAngle = 0;
    }

    static void CornellBox() throws IOException
    {
        Lambertian red   = new Lambertian(new Vector3(.65f, .05f, .05f));
        Lambertian white = new Lambertian(new Vector3(.73f, .73f, .73f));
        Lambertian green = new Lambertian(new Vector3(.12f, .45f, .15f));
        DiffuseLight light = new DiffuseLight(new Vector3(1, 1, 1));

        // Right face (x = 555)
        world.add(new Triangle(new Vector3(555, 0, 0), new Vector3(555, 555, 0), new Vector3(555, 0, 555), green));
        world.add(new Triangle(new Vector3(555, 555, 555), new Vector3(555, 0, 555), new Vector3(555, 555, 0), green));

        // Left face (x = 0)
        world.add(new Triangle(new Vector3(0, 0, 0), new Vector3(0, 0, 555), new Vector3(0, 555, 0), red));
        world.add(new Triangle(new Vector3(0, 555, 555), new Vector3(0, 555, 0), new Vector3(0, 0, 555), red));

        // Floor (y = 0)
        world.add(new Triangle(new Vector3(0, 0, 0), new Vector3(555, 0, 0), new Vector3(0, 0, 555), white));
        world.add(new Triangle(new Vector3(555, 0, 555), new Vector3(0, 0, 555), new Vector3(555, 0, 0), white));

        // Ceiling (y = 555)
        world.add(new Triangle(new Vector3(555, 555, 555), new Vector3(0, 555, 555), new Vector3(555, 555, 0), light));
        world.add(new Triangle(new Vector3(0, 555, 0), new Vector3(555, 555, 0), new Vector3(0, 555, 555), light));

        // Back wall (z = 555)
        world.add(new Triangle(new Vector3(0, 0, 555), new Vector3(555, 0, 555), new Vector3(0, 555, 555), white));
        world.add(new Triangle(new Vector3(555, 555, 555), new Vector3(0, 555, 555), new Vector3(555, 0, 555), white));

        Object monkey = new Object("/Users/florianlerch/Raytracer/Models/monkey.obj", red, new Vector3(278, 200, 200), new Vector3(0, 0, (float)Math.PI / 2), new Vector3(100, 100, 100));
        //Object cube2 = new Object("/Users/florianlerch/Raytracer/Models/GrassCube.obj", red);
        world.addAll(monkey.triangles);


        cam.aspectRatio      = 1.0f;
        cam.imageWidth       = 300;
        cam.samplesPerPixel = 100;
        cam.max_depth         = 50;
        cam.background        = new Vector3(0,0,0);

        cam.vfov     = 40;
        cam.lookFrom = new Vector3(278, 278, -800);
        cam.lookAt   = new Vector3(278, 278, 0);
        cam.vup      = new Vector3(0,1,0);

        cam.defocusAngle = 0;
    }

    static void SimpleLight() throws IOException
    {
        ImageTexture tex = new ImageTexture("/Users/florianlerch/Raytracer/Textures/earthmap.jpg");
        world.add(new Sphere(new Vector3(0,-1000,0), 1000, new Lambertian(new Vector3(0.5f, 0.5f, 0.5f))));
        world.add(new Sphere(new Vector3(0,2,0), 2, new Lambertian(tex)));

        DiffuseLight difflight = new DiffuseLight(new Vector3(4,4,4));
        world.add(new Sphere(new Vector3(0, 7, 0), 2, difflight));

        cam.aspectRatio = 16.0f / 9.0f;
        cam.imageWidth = 400;
        cam.samplesPerPixel = 100;
        cam.max_depth = 50;

        cam.vfov = 20;
        cam.lookFrom = new Vector3(26,3,6);
        cam.lookAt = new Vector3(0,2,0);
        cam.vup = new Vector3(0,1,0);

        cam.background = new Vector3(0,0,0);
        cam.defocusAngle = 0;
    }

    static void Earth() throws IOException
    {
        ImageTexture earthTexture = new ImageTexture("/Users/florianlerch/Raytracer/Textures/earthmap.jpg");
        Material earthSurface = new Lambertian(earthTexture);
        world.add(new Sphere(new Vector3(0, 0, 0), 2, earthSurface));

        cam.aspectRatio = 16.0f / 9.0f;
        cam.imageWidth = 400;
        cam.samplesPerPixel = 100;
        cam.max_depth = 50;

        cam.vfov = 20;
        cam.lookFrom = new Vector3(0,0,12);
        cam.lookAt = new Vector3(0,0,0);
        cam.vup = new Vector3(0,1,0);

        cam.defocusAngle = 0;
    }

    static void CheckeredSpheres()
    {
        Texture checker = new CheckerTexture(0.32f, new Vector3(.2f, .3f, .1f), new Vector3(.9f, .9f, .9f));
        world.add(new Sphere(new Vector3(0, -10, 0), 10, new Lambertian(checker)));
        world.add(new Sphere(new Vector3(0, 10, 0), 10, new Lambertian(checker)));

        cam.aspectRatio = 16.0f / 9.0f;
        cam.imageWidth = 400;
        cam.samplesPerPixel = 100;
        cam.max_depth = 50;

        cam.vfov = 20;
        cam.lookFrom = new Vector3(13,2,3);
        cam.lookAt = new Vector3(0,0,0);
        cam.vup = new Vector3(0,1,0);

        cam.defocusAngle = 0;
    }

    static void RandomWorld()
    {
        Material ground_material = new Lambertian(new Vector3(0.5f, 0.5f, 0.5f));
        world.add(new Sphere(new Vector3(0, -1000, 0), 1000, ground_material));

        for (int a = -11; a < 11; a++)
        {
            for (int b = -11; b < 11; b++)
            {
                float chooseMat = (float)Math.random();
                Vector3 center = new Vector3(a + 0.9f * (float)Math.random(), 0.2f, b + 0.9f * (float)Math.random());

                if (center.sub(new Vector3(4, 0.2f, 0)).length() > 0.9f)
                {
                    Material sphereMaterial;

                    if (chooseMat < 0.8f)
                    {
                        Vector3 albedo = randomVec().mult(randomVec());
                        sphereMaterial = new Lambertian(albedo);
                        world.add(new Sphere(center, 0.2f, sphereMaterial));
                    }
                    else if (chooseMat < 0.95f)
                    {
                        Vector3 albedo = randomVec(0.5f, 1);
                        float fuzz = random(0, 0.5f);
                        sphereMaterial = new Metal(albedo, fuzz);
                        world.add(new Sphere(center, 0.2f, sphereMaterial));
                    }
                    else
                    {
                        sphereMaterial = new Dielectric(1.5f);
                        world.add(new Sphere(center, 0.2f, sphereMaterial));
                    }
                }
            }
        }

        Material material1 = new Dielectric(1.5f);
        world.add(new Sphere(new Vector3(0, 1, 0), 1.0f, material1));

        Material material2 = new Lambertian(new Vector3(0.4f, 0.2f, 0.1f));
        world.add(new Sphere(new Vector3(-4, 1, 0), 1.0f, material2));

        Material material3 = new Metal(new Vector3(0.7f, 0.6f, 0.5f), 0);
        world.add(new Sphere(new Vector3(4, 1, 0), 1.0f, material3));
    }
}
