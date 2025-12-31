import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Utilities extends Interval
{
    public static class Vector3 
    {
        public float x, y, z;

        public Vector3(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public Vector3 add(Vector3 other) { return new Vector3(this.x + other.x, this.y + other.y, this.z + other.z); }
        public Vector3 sub(Vector3 other) { return new Vector3(this.x - other.x, this.y - other.y, this.z - other.z);  }

        public Vector3 mult(float scalar) { return new Vector3(this.x * scalar, this.y * scalar, this.z * scalar); }
        public Vector3 mult(Vector3 other) { return new Vector3(this.x * other.x, this.y * other.y, this.z * other.z); }
        public Vector3 div(float scalar) { return new Vector3(this.x / scalar, this.y / scalar, this.z / scalar); }
        public Vector3 div(Vector3 other) { return new Vector3(this.x / other.x, this.y / other.y, this.z / other.z); }

        public float lengthSquared() { return x * x + y * y + z * z; }
        public float length() { return (float)Math.sqrt(lengthSquared()); }
        public Vector3 normalize() { return this.div(length()); }
        public boolean nearZero()
        {
            float s = 0.00001f;
            return x < s && y < s && z < s;
        }
    }
    
    public static float dot(Vector3 a, Vector3 b) { return a.x * b.x + a.y * b.y + a.z * b.z; }
    public Vector3 cross(Vector3 a, Vector3 b) 
    {
        float cx = a.y * b.z - a.z * b.y;
        float cy = a.z * b.x - a.x * b.z;
        float cz = a.x * b.y - a.y * b.x;

        return new Vector3(cx, cy, cz);
    }

    public static float random(float min, float max) { return min + (max - min) * (float)Math.random(); }

    public static Vector3 randomVec() { return new Vector3((float)Math.random(), (float)Math.random(), (float)Math.random()); }
    public static Vector3 randomVec(float min, float max) { return new Vector3(random(min,max), random(min,max), random(min,max)); }

    public static float degreeToRadians(float degree) { return degree * (float)Math.PI / 180f; }

    public static Vector3 RandomInUnitDisc()
    {
        while (true)
        {
            Vector3 p = new Vector3(random(-1, 1), random(-1, 1), 0);
            if (p.lengthSquared() < 1) return p;
        }
    }

    public static Vector3 RandomUnitVector()
    {
        while (true)
        {
            Vector3 p = randomVec(-1, 1);
            float lensq = p.lengthSquared();
            if (lensq <= 1) return p.div((float)Math.sqrt(lensq));
        }
    }

    public static Vector3 RandomVectorOnHemisphere(Vector3 normal)
    {
        Vector3 onUnitSphere = RandomUnitVector();
        if (dot(onUnitSphere, normal) > 0.0f) return onUnitSphere;
        else return onUnitSphere.mult(-1);
    }

    public static Vector3 Reflect(Vector3 v, Vector3 n)
    {
        return v.sub(n.mult(2*dot(v,n)));
    }

    public static Vector3 Refract(Vector3 uv, Vector3 n, float etaiOverEtat)
    {
        float cosTheta = Math.min(dot(uv.mult(-1), n), 1.0f);
        Vector3 rOutPerp = uv.add(n.mult(cosTheta)).mult(etaiOverEtat);
        Vector3 rOutParallel = n.mult((float)-Math.sqrt(Math.abs(1.0f - rOutPerp.lengthSquared())));
        return rOutPerp.add(rOutParallel);
    }

    public static class Ray
    {
        public Vector3 orig, dir;
        public float time;

        public Ray(Vector3 orig, Vector3 dir)
        {
            this.orig = orig;
            this.dir = dir;
        }

        public Vector3 at(float t)
        {
            return orig.add(dir.mult(t));
        }
    }

    static float linearToGamma(float linearComponent)  
    { 
        if (linearComponent > 0) return (float)Math.sqrt(linearComponent); 
        else return 0;
    }

    public static void WriteColor(FileWriter writer, Vector3 color) throws IOException
    {
        float r = color.x;
        float g = color.y;
        float b = color.z;

        r = linearToGamma(r);
        g = linearToGamma(g);
        b = linearToGamma(b);

        Interval intensity = new Interval(0.000f, 0.999f);
        int ir = (int)(256 * intensity.clamp(r));
        int ig = (int)(256 * intensity.clamp(g));
        int ib = (int)(256 * intensity.clamp(b));

        writer.write(ir + " " + ig + " " + ib + "\n");
    }

    static void CreateImageFile()
    {
        try 
        {
            File img = new File("Image.ppm");
            if (img.createNewFile()) {
                System.out.println("File created: " + img.getName());
            } 
            else {
                System.out.println("File already exists.");
            }
        } 
        catch (IOException e) 
        {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    } 
}

class Interval
{
    float min;
    float max;

    public Interval()
    {
        min = -100000f;
        max = 100000f;
    }

    public Interval(float min, float max)
    {
        this.min = min;
        this.max = max;
    }

    public float size() { return max - min; }
    public boolean contains(float x) { return min <= x && max >= x; }
    public boolean surrounds(float x) { return min < x && max > x; }
    public float clamp(float x) 
    { 
        if (x < min) return min;
        if (x > max) return max;
        return x;
    }

    public Interval expand(float delta)
    {
        float padding = delta / 2;
        return new Interval(min - padding, max + padding);
    }

    public static Interval merge(Interval a, Interval b) 
    {
        float newMin = Math.min(a.min, b.min);
        float newMax = Math.max(a.max, b.max);
        return new Interval(newMin, newMax);
    }
}