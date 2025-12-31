import java.util.ArrayList;
import java.util.List;

public abstract class Hittable extends Utilities
{
    abstract HitRecord hit(Ray r, Interval ray_t);
}

class Sphere extends Hittable
{
    Vector3 center;
    float radius;
    Material mat;

    public Sphere (Vector3 center, float radius, Material mat)
    {
        this.center = center;
        this.radius = radius;
        this.mat = mat;
    }

    public HitRecord hit(Ray r, Interval ray_t)
    {
        Vector3 oc = center.sub(r.orig);
        float a = r.dir.lengthSquared();
        float h = dot(r.dir, oc);
        float c = oc.lengthSquared() - radius * radius;
        float discriminant = h*h - a*c;

        if (discriminant < 0) return null;
        
        float sqrtd = (float)Math.sqrt(discriminant);
        float root = (h - sqrtd) / a;
        if (root <= ray_t.min || root >= ray_t.max)
        {
            root = (h + sqrtd) / a;
            if (root <= ray_t.min || root >= ray_t.max) return null;
        }

        HitRecord rec = new HitRecord();

        rec.t = root;
        rec.p = r.at(rec.t);
        Vector3 outwardNormal = rec.p.sub(center).div(radius);
        rec.SetFaceNormal(r, outwardNormal);
        Vector3 uvs = GetSphereUVs(outwardNormal);
        rec.u = uvs.x;
        rec.v = uvs.y;
        rec.mat = mat;

        return rec;
    }

    public static Vector3 GetSphereUVs(Vector3 p)
    {
        float theta = (float)Math.acos(-p.y);
        float phi = (float)(Math.atan2(-p.z, p.x) + Math.PI);

        float u = phi / (2 * (float)Math.PI);
        float v = theta / (float)Math.PI;

        return new Vector3(u, v, 0);
    } 
}

class Object extends Utilities
{
    List<Hittable> triangles = new ArrayList<Hittable>();
    Vector3[] basisVectors;
    public Object (String filename, Material mat, Vector3 position, Vector3 rotation, Vector3 scale)
    {
        Vector3[] obj = ObjParser.LoadObjFile(filename);
        basisVectors = GetBasisVectors(rotation.x, rotation.y, rotation.z);
        for (int i = 0; i < obj.length; i+=3)
        {
            Vector3 p1 = ToWorldPoint(obj[i + 0], position, scale, basisVectors);
            Vector3 p2 = ToWorldPoint(obj[i + 1], position, scale, basisVectors);
            Vector3 p3 = ToWorldPoint(obj[i + 2], position, scale, basisVectors);
            triangles.add(new Triangle(p1, p2, p3, mat));
        }
    }

    public static Vector3 ToWorldPoint(Vector3 p, Vector3 position, Vector3 scale, Vector3[] basisVectors)
    {
        Vector3 ihat = basisVectors[0].mult(scale.x);
        Vector3 jhat = basisVectors[1].mult(scale.y);
        Vector3 khat = basisVectors[2].mult(scale.z);
        return TransformVector(ihat, khat, jhat, p).add(position);
    }

    static Vector3[] GetBasisVectors(float Roll, float Yaw, float Pitch)
    {
        Vector3 ihat_yaw = new Vector3((float)Math.cos(Yaw), 0, (float)Math.sin(Yaw));
        Vector3 jhat_yaw = new Vector3(0,1,0);
        Vector3 khat_yaw = new Vector3((float)-Math.sin(Yaw), 0, (float)Math.cos(Yaw));

        Vector3 ihat_pitch = new Vector3(1,0,0);
        Vector3 jhat_pitch = new Vector3(0,(float)Math.cos(Pitch),(float)-Math.sin(Pitch));
        Vector3 khat_pitch = new Vector3(0, (float)Math.sin(Pitch), (float)Math.cos(Pitch));

        Vector3 ihat_roll = new Vector3((float)Math.cos(Roll),(float)Math.sin(Roll),0);
        Vector3 jhat_roll = new Vector3((float)-Math.sin(Roll),(float)Math.cos(Roll),0);
        Vector3 khat_roll = new Vector3(0, 0, 1);

        Vector3 ihat_pitchYaw = TransformVector(ihat_yaw, jhat_yaw, khat_yaw, ihat_pitch);
        Vector3 jhat_pitchYaw = TransformVector(ihat_yaw, jhat_yaw, khat_yaw, jhat_pitch);
        Vector3 khat_pitchYaw = TransformVector(ihat_yaw, jhat_yaw, khat_yaw, khat_pitch);

        Vector3 ihat = TransformVector(ihat_pitchYaw, jhat_pitchYaw, khat_pitchYaw, ihat_roll);
        Vector3 jhat = TransformVector(ihat_pitchYaw, jhat_pitchYaw, khat_pitchYaw, jhat_roll);
        Vector3 khat = TransformVector(ihat_pitchYaw, jhat_pitchYaw, khat_pitchYaw, khat_roll);

        return new Vector3[] {ihat, jhat, khat};
    }

    static Vector3 TransformVector(Vector3 ihat, Vector3 jhat, Vector3 khat, Vector3 p)
    {
        Vector3 x = ihat.mult(p.x);
        Vector3 y = jhat.mult(p.y);
        Vector3 z = khat.mult(p.z);
        return x.add(y).add(z);
    }
}

class Triangle extends Hittable
{
    Vector3 Q, u, v;
    Material mat;
    public AABB bbox;
    Vector3 normal;
    float D;
    Vector3 w;

    public Triangle(Vector3 p1, Vector3 p2, Vector3 p3, Material mat)
    {
        this.Q = p1;
        this.u = p2.sub(p1);
        this.v = p3.sub(p1);
        this.mat = mat;

        Vector3 n = cross(u,v);
        normal = n.normalize();
        D = dot(normal, Q);
        w = n.div(dot(n,n));

        SetBoundingBox();
    }

    void SetBoundingBox()
    {
        AABB bboxDiagonal1 = new AABB(Q, Q.add(u).add(v));
        AABB bboxDiagonal2 = new AABB(Q.add(u), Q.add(v));
        bbox = new AABB(bboxDiagonal1, bboxDiagonal2);
    }

    public HitRecord hit(Ray r, Interval ray_t)
    {
        float denom = dot(normal, r.dir);
        if (Math.abs(denom) < 0.000001f) return null;

        float t = (D - dot(normal, r.orig)) / denom;
        if (!ray_t.contains(t)) return null;

        Vector3 intersection = r.at(t);
        Vector3 planarHitptVector = intersection.sub(Q);
        float alpha = dot(w, cross(planarHitptVector, v));
        float beta = dot(w, cross(u, planarHitptVector));
        if (!IsInterior(alpha, beta)) return null;

        HitRecord rec = new HitRecord();
        rec.t = t;
        rec.p = intersection;
        rec.mat = mat;
        rec.SetFaceNormal(r, normal);
        rec.u = alpha;
        rec.v = beta;
        return rec;
    }

    boolean IsInterior(float a, float b)
    {
        //Interval unitInterval = new Interval(0,1);
        //if (unitInterval.contains(a) && unitInterval.contains(b)) return true;
        if (a > 0 && b > 0 && a+b < 1) return true;
        return false;
    }
}

class AABB extends Utilities
{
    Interval x, y, z;
    public AABB(Interval x, Interval y, Interval z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        PadToMinimums();
    }

    public AABB(Vector3 a, Vector3 b)
    {
        // Treat the two points a and b as extrema for the bounding box, so we don't require a
        // particular minimum/maximum coordinate order.

        x = new Interval(Math.min(a.x, b.x), Math.max(a.x, b.x));
        y = new Interval(Math.min(a.y, b.y), Math.max(a.y, b.y));
        z = new Interval(Math.min(a.z, b.z), Math.max(a.z, b.z));

        PadToMinimums();
    }

    public AABB(AABB a, AABB b) {
        // Builds a box from two boxes
        this.x = Interval.merge(a.x, b.x);
        this.y = Interval.merge(a.y, b.y);
        this.z = Interval.merge(a.z, b.z);
    }

    void PadToMinimums()
    {
        float delta = 0.0001f;
        if (x.size() < delta) x = x.expand(delta);
        if (y.size() < delta) y = y.expand(delta);
        if (z.size() < delta) z = z.expand(delta);
    }
}

class HitRecord extends Utilities
{
    public Vector3 p;
    public Vector3 normal;
    public float t;
    public boolean frontFace;
    public Material mat;
    public float u;
    public float v;

    public void SetFaceNormal(Ray r, Vector3 outwardNormal)
    {
        //The parameter outwardNormal is always assumed to be of unit length (normalized)
        frontFace = dot(r.dir, outwardNormal) < 0;
        normal = frontFace ? outwardNormal : outwardNormal.mult(-1);
    }
}
