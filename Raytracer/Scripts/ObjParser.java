import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
// Import the Scanner class to read text files
import java.util.*;

public class ObjParser extends Utilities 
{
    public static String[] GetLines(String fileName)
    {
        List<String> stringArray = new ArrayList<String>();
        try 
        {
            File myObj = new File(fileName);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) 
            {
                String data = myReader.nextLine();
                stringArray.add(data);
            }
            myReader.close();
        } 
        catch (FileNotFoundException e) 
        {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return stringArray.toArray(new String[stringArray.size()]);
    }

    public static Vector3[] LoadObjFile(String fileName)
    {
        List<Vector3> allpoints = new ArrayList<Vector3>();
        List<Vector3> trianglePoints = new ArrayList<Vector3>();

        for(String line : GetLines(fileName))
        {
            if (line.substring(0,2).equals("v "))
            {
                String[] axesString = line.substring(2).split(" ");
                //System.out.println(axesString[0] + ", " + axesString[1] + ", " + axesString[2]);
                float[] axes = new float[3];

                for(int i = 0; i < 3; i++) { axes[i] = Float.parseFloat(axesString[i]); }
                allpoints.add(new Vector3(axes[0], axes[1], axes[2]));
            }

            else if (line.substring(0,2).equals("f "))
            {
                String[] faceIndexGroups = line.substring(2).split(" ");

                for(int i = 0; i < faceIndexGroups.length; i++)
                {
                    String[] indexGroupString = faceIndexGroups[i].split("/");
                    int[] indexGroup = new int[3];
                    for(int j = 0; j < 3; j++) { indexGroup[j] = Integer.valueOf(indexGroupString[j]); }

                    //System.out.println(indexGroup[0] + ", " + indexGroup[1] + ", " + indexGroup[2]);

                    int pointIndex = indexGroup[0] - 1;
                    if (i >= 3) trianglePoints.add(trianglePoints.get(trianglePoints.size() - (3 * i - 6)));
                    if (i >= 3) trianglePoints.add(trianglePoints.get(trianglePoints.size() - 2));
                    trianglePoints.add(allpoints.get(pointIndex));
                }
            }
        }

        return trianglePoints.toArray(new Vector3[trianglePoints.size()]);
    }

    /*public static Vector2[] LoadTextureCoords(String fileName)
    {
        List<Vector2> textureCoords = new ArrayList<Vector2>();
        for(String line : GetLines(fileName))
        {
            if (line.substring(0,3).equals("vt "))
            {
                String[] coordsStr = line.substring(3).split(" ");
                float[] coords = new float[3];
                for(int i = 0; i < 2; i++) { coords[i] = Float.parseFloat(coordsStr[i]); }
                textureCoords.add(new Vector2(coords[0], coords[1]));
            }
        }

        List<Vector2> allTexCoords = new ArrayList<Vector2>();
        for(String line : GetLines(fileName))
        {
            if (line.substring(0,2).equals("f "))
            {
                String[] faceIndexGroups = line.substring(2).split(" ");

                for(int i = 0; i < faceIndexGroups.length; i++)
                {
                    String[] indexGroupString = faceIndexGroups[i].split("/");
                    int[] indexGroup = new int[3];
                    for(int j = 0; j < 3; j++) { indexGroup[j] = Integer.valueOf(indexGroupString[j]); }

                    int pointIndex = indexGroup[1] - 1;
                    if (i >= 3) allTexCoords.add(allTexCoords.get(allTexCoords.size() - (3 * i - 6)));
                    if (i >= 3) allTexCoords.add(allTexCoords.get(allTexCoords.size() - 2));
                    allTexCoords.add(textureCoords.get(pointIndex));
                }
            }
        }
        return allTexCoords.toArray(new Vector2[allTexCoords.size()]);
    }

    public static Vector3[] LoadNormals(String fileName)
    {
        List<Vector3> normals = new ArrayList<Vector3>();
        for(String line : GetLines(fileName))
        {
            if (line.substring(0,3).equals("vn "))
            {
                String[] coordsStr = line.substring(3).split(" ");
                float[] coords = new float[3];
                for(int i = 0; i < 3; i++) { coords[i] = Float.parseFloat(coordsStr[i]); }
                normals.add(new Vector3(coords[0], coords[1], coords[2]));
            }
        }

        List<Vector3> allNormals = new ArrayList<Vector3>();
        for(String line : GetLines(fileName))
        {
            if (line.substring(0,2).equals("f "))
            {
                String[] faceIndexGroups = line.substring(2).split(" ");

                for(int i = 0; i < faceIndexGroups.length; i++)
                {
                    String[] indexGroupString = faceIndexGroups[i].split("/");
                    int[] indexGroup = new int[3];
                    for(int j = 0; j < 3; j++) { indexGroup[j] = Integer.valueOf(indexGroupString[j]); }

                    int pointIndex = indexGroup[2] - 1;
                    if (i >= 3) allNormals.add(allNormals.get(allNormals.size() - (3 * i - 6)));
                    if (i >= 3) allNormals.add(allNormals.get(allNormals.size() - 2));
                    allNormals.add(normals.get(pointIndex));
                }
            }
        }
        return allNormals.toArray(new Vector3[allNormals.size()]);
    }*/
}