public abstract class Material extends Utilities
{
    public MaterialScatter scatter(Ray r, HitRecord rec) { return null; };
    public Vector3 emitted(float u, float v, Vector3 p) { return new Vector3(0, 0, 0); }
}

class Lambertian extends Material
{
    Texture tex;

    public Lambertian(Vector3 albedo)
    {
        this.tex = new SolidColor(albedo);
    }

    public Lambertian(Texture tex)
    {
        this.tex = tex;
    }

    public MaterialScatter scatter(Ray r, HitRecord rec)
    {
        Vector3 scatterDirection = rec.normal.add(RandomUnitVector());
        if (scatterDirection.nearZero()) scatterDirection = rec.normal;

        Ray scattered = new Ray(rec.p, scatterDirection);
        Vector3 attenuation = tex.Value(rec.u, rec.v, rec.p);
        return new MaterialScatter(scattered, attenuation, true);
    }
} 

class Metal extends Material
{
    Texture tex;
    float fuzz;

    public Metal(Vector3 albedo, float fuzz)
    {
        this.tex = new SolidColor(albedo);
        this.fuzz = fuzz < 1 ? fuzz : 1;
    }

    public Metal(Texture tex, float fuzz)
    {
        this.tex = tex;
        this.fuzz = fuzz < 1 ? fuzz : 1;
    }

    public MaterialScatter scatter(Ray r, HitRecord rec)
    {
        Vector3 reflected = Reflect(r.dir, rec.normal);
        reflected = reflected.normalize().add(RandomUnitVector().mult(fuzz));
        Ray scattered = new Ray(rec.p, reflected);
        Vector3 attenuation = tex.Value(rec.u, rec.v, rec.p);
        return new MaterialScatter(scattered, attenuation, true);
    }
}

class Dielectric extends Material
{
    float reftactionIndex;

    public Dielectric(float refractionIndex)
    {
        this.reftactionIndex = refractionIndex;
    }

    public MaterialScatter scatter(Ray r, HitRecord rec)
    {
        Vector3 attenuation = new Vector3(1, 1, 1);
        float ri = rec.frontFace ? (1.0f / reftactionIndex) : reftactionIndex;

        Vector3 unitDirection = r.dir.normalize();
        float cosTheta = Math.min(dot(unitDirection.mult(-1), rec.normal), 1.0f);
        float sinTheta = (float)Math.sqrt(1.0f - cosTheta * cosTheta);
        boolean cannotRefract = ri * sinTheta > 1.0f;
        Vector3 direction;

        if (cannotRefract || Reflectance(cosTheta, ri) > Math.random()) direction = Reflect(unitDirection, rec.normal);
        else direction = Refract(unitDirection, rec.normal, ri);

        Ray scattered = new Ray(rec.p, direction);
        return new MaterialScatter(scattered, attenuation, true);
    }

    static float Reflectance(float cosine, float refractionIndex)
    {
        float r0 = (1 - refractionIndex) / (1 + refractionIndex);
        r0 = r0 * r0;
        return r0 + (1-r0) * (float)Math.pow((1-cosine), 5);
    }
}

class DiffuseLight extends Material
{
    Texture tex;

    public DiffuseLight(Texture tex)
    {
        this.tex = tex;
    }

    public DiffuseLight(Vector3 emit)
    {
        this.tex = new SolidColor(emit);
    }

    public Vector3 emitted(float u, float v, Vector3 p)
    {
        return tex.Value(u, v, p);
    }
}

class MaterialScatter extends Utilities
{
    Ray scattered;
    Vector3 attenuation;
    boolean scatter;

    public MaterialScatter(Ray scattered, Vector3 attenuation, boolean scatter)
    {
        this.scattered = scattered;
        this.attenuation = attenuation;
        this.scatter = scatter;
    }
}