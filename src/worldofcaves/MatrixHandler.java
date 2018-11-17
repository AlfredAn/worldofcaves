package worldofcaves;

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import static org.lwjgl.opengl.GL20.*;

public class MatrixHandler
{
    private final Matrix4f projection, view, model, MVP, biasMVP;
    private final FloatBuffer matrixBuffer;
    private boolean isUpdated = false, isBiasUpdated = false;
    private final float[][] frustum = new float[6][4];
    
    public static final Vector3f vec0 = new Vector3f();
    public static final Vector3f vec1 = new Vector3f(1, 1, 1);
    
    private static final Matrix4f biasMatrix = biasMatrix();
    
    public MatrixHandler()
    {
        projection = new Matrix4f();
        view = new Matrix4f();
        model = new Matrix4f();
        MVP = new Matrix4f();
        biasMVP = new Matrix4f();
        
        matrixBuffer = BufferUtils.createFloatBuffer(16);
    }
    
    public void setProjection(Matrix4f mat)
    {
        projection.load(mat);
        isUpdated = false;
    }
    public void setProjection(double fov, double aspectRatio, double zNear, double zFar)
    {
        projectionMatrix(fov, aspectRatio, zNear, zFar, projection);
        isUpdated = false;
    }
    public static Matrix4f projectionMatrix(double fov, double aspectRatio, double zNear, double zFar, Matrix4f dest)
    {
        Matrix4f mat = dest;
        if (mat == null)
        {
            mat = new Matrix4f();
        }
        else
        {
            mat.setIdentity();
        }
        
        double yScale = Util.cotan(Math.toRadians(fov / 2));
        double xScale = yScale / aspectRatio;
        double frustumLength = zFar - zNear;
        
        mat.m00 = (float)xScale;
        mat.m11 = (float)yScale;
        mat.m22 = (float)-((zFar + zNear) / frustumLength);
        mat.m23 = -1;
        mat.m32 = (float)-((2 * zNear * zFar) / frustumLength);
        mat.m33 = 0;
        
        return mat;
    }
    
    public void setProjectionOrtho(double x1, double y1, double z1, double x2, double y2, double z2)
    {
        projectionMatrixOrtho(x1, y1, z1, x2, y2, z2, projection);
        isUpdated = false;
    }
    public static Matrix4f projectionMatrixOrtho(double x1, double y1, double z1, double x2, double y2, double z2, Matrix4f dest)
    {
        Matrix4f mat = dest;
        if (mat == null)
        {
            mat = new Matrix4f();
        }
        else
        {
            mat.setIdentity();
        }
        
        float l = (float)x1;
        float b = (float)y1;
        float n = (float)z1;
        float r = (float)x2;
        float t = (float)y2;
        float f = (float)z2;
        
        mat.m00 = 2/(r-l);
        mat.m03 = -(r+l)/(r-l);
        mat.m11 = 2/(t-b);
        mat.m13 = -(t+b)/(t-b);
        mat.m22 = -2/(f-n);
        mat.m23 = -(f+n)/(f-n);
        
        return mat;
    }
    
    public void setView(Matrix4f mat)
    {
        view.load(mat);
        isUpdated = false;
    }
    public void setView(Vector3f pos, Vector3f rot)
    {
        viewMatrix(pos.x, pos.y, pos.z, rot.x, rot.y, rot.z, view);
        isUpdated = false;
    }
    public Matrix4f setView(
            float xPos, float yPos, float zPos,
            float xRot, float yRot, float zRot)
    {
        viewMatrix(xPos, yPos, zPos, xRot, yRot, zRot, view);
        isUpdated = false;
        return view;
    }
    
    public static Matrix4f viewMatrix(Vector3f pos, Vector3f rot, Matrix4f dest)
    {
        return viewMatrix(pos.x, pos.y, pos.z, rot.x, rot.y, rot.z, dest);
    }
    public static Matrix4f viewMatrix(
            float xPos, float yPos, float zPos,
            float xRot, float yRot, float zRot, Matrix4f dest)
    {
        Matrix4f mat = dest;
        
        if (mat == null)
        {
            mat = new Matrix4f();
        }
        else
        {
            mat.setIdentity();
        }
        
        Matrix4f.rotate(xRot, new Vector3f(1, 0, 0),
                mat, mat);
        Matrix4f.rotate(yRot, new Vector3f(0, 1, 0),
                mat, mat);
        Matrix4f.rotate(zRot, new Vector3f(0, 0, 1),
                mat, mat);
        
        Matrix4f.translate(new Vector3f(-xPos, -yPos, -zPos), mat, mat);
        
        return mat;
    }
    
    public void updateFrustum()
    {
        Matrix4f mat = Matrix4f.mul(projection, view, null);
        float[] clip = new float[16];
        
        clip[0]  = mat.m00;
        clip[1]  = mat.m01;
        clip[2]  = mat.m02;
        clip[3]  = mat.m03;
        clip[4]  = mat.m10;
        clip[5]  = mat.m11;
        clip[6]  = mat.m12;
        clip[7]  = mat.m13;
        clip[8]  = mat.m20;
        clip[9]  = mat.m21;
        clip[10] = mat.m22;
        clip[11] = mat.m23;
        clip[12] = mat.m30;
        clip[13] = mat.m31;
        clip[14] = mat.m32;
        clip[15] = mat.m33;
        
        /* Extract the numbers for the RIGHT plane */
        frustum[0][0] = clip[ 3] - clip[ 0];
        frustum[0][1] = clip[ 7] - clip[ 4];
        frustum[0][2] = clip[11] - clip[ 8];
        frustum[0][3] = clip[15] - clip[12];

        /* Normalize the result */
        float t = (float)Math.sqrt( frustum[0][0] * frustum[0][0] + frustum[0][1] * frustum[0][1] + frustum[0][2] * frustum[0][2] );
        frustum[0][0] /= t;
        frustum[0][1] /= t;
        frustum[0][2] /= t;
        frustum[0][3] /= t;

        /* Extract the numbers for the LEFT plane */
        frustum[1][0] = clip[ 3] + clip[ 0];
        frustum[1][1] = clip[ 7] + clip[ 4];
        frustum[1][2] = clip[11] + clip[ 8];
        frustum[1][3] = clip[15] + clip[12];

        /* Normalize the result */
        t = (float)Math.sqrt( frustum[1][0] * frustum[1][0] + frustum[1][1] * frustum[1][1] + frustum[1][2] * frustum[1][2] );
        frustum[1][0] /= t;
        frustum[1][1] /= t;
        frustum[1][2] /= t;
        frustum[1][3] /= t;

        /* Extract the BOTTOM plane */
        frustum[2][0] = clip[ 3] + clip[ 1];
        frustum[2][1] = clip[ 7] + clip[ 5];
        frustum[2][2] = clip[11] + clip[ 9];
        frustum[2][3] = clip[15] + clip[13];

        /* Normalize the result */
        t = (float)Math.sqrt( frustum[2][0] * frustum[2][0] + frustum[2][1] * frustum[2][1] + frustum[2][2] * frustum[2][2] );
        frustum[2][0] /= t;
        frustum[2][1] /= t;
        frustum[2][2] /= t;
        frustum[2][3] /= t;

        /* Extract the TOP plane */
        frustum[3][0] = clip[ 3] - clip[ 1];
        frustum[3][1] = clip[ 7] - clip[ 5];
        frustum[3][2] = clip[11] - clip[ 9];
        frustum[3][3] = clip[15] - clip[13];

        /* Normalize the result */
        t = (float)Math.sqrt( frustum[3][0] * frustum[3][0] + frustum[3][1] * frustum[3][1] + frustum[3][2] * frustum[3][2] );
        frustum[3][0] /= t;
        frustum[3][1] /= t;
        frustum[3][2] /= t;
        frustum[3][3] /= t;

        /* Extract the FAR plane */
        frustum[4][0] = clip[ 3] - clip[ 2];
        frustum[4][1] = clip[ 7] - clip[ 6];
        frustum[4][2] = clip[11] - clip[10];
        frustum[4][3] = clip[15] - clip[14];

        /* Normalize the result */
        t = (float)Math.sqrt( frustum[4][0] * frustum[4][0] + frustum[4][1] * frustum[4][1] + frustum[4][2] * frustum[4][2] );
        frustum[4][0] /= t;
        frustum[4][1] /= t;
        frustum[4][2] /= t;
        frustum[4][3] /= t;

        /* Extract the NEAR plane */
        frustum[5][0] = clip[ 3] + clip[ 2];
        frustum[5][1] = clip[ 7] + clip[ 6];
        frustum[5][2] = clip[11] + clip[10];
        frustum[5][3] = clip[15] + clip[14];

        /* Normalize the result */
        t = (float)Math.sqrt( frustum[5][0] * frustum[5][0] + frustum[5][1] * frustum[5][1] + frustum[5][2] * frustum[5][2] );
        frustum[5][0] /= t;
        frustum[5][1] /= t;
        frustum[5][2] /= t;
        frustum[5][3] /= t;
    }
    
    public boolean pointInFrustum(float x, float y, float z)
    {
       for (int p = 0; p < 6; p++)
       {
            if (frustum[p][0] * x + frustum[p][1] * y + frustum[p][2] * z + frustum[p][3] <= 0)
            {
                return false;
            }
       }
       
       return true;
    }
    
    public boolean sphereInFrustum(float x, float y, float z, float radius)
    {
       for (int p = 0; p < 6; p++)
       {
            if (frustum[p][0] * x + frustum[p][1] * y + frustum[p][2] * z + frustum[p][3] <= -radius)
            {
                return false;
            }
       }
       
       return true;
    }
    
    public boolean cubeInFrustum(float x, float y, float z, float size)
    {
       for (int p = 0; p < 6; p++)
       {
            if (frustum[p][0] * (x - size) + frustum[p][1] * (y - size) + frustum[p][2] * (z - size) + frustum[p][3] > 0)
                continue;
            if (frustum[p][0] * (x + size) + frustum[p][1] * (y - size) + frustum[p][2] * (z - size) + frustum[p][3] > 0)
                continue;
            if (frustum[p][0] * (x - size) + frustum[p][1] * (y + size) + frustum[p][2] * (z - size) + frustum[p][3] > 0)
                continue;
            if (frustum[p][0] * (x + size) + frustum[p][1] * (y + size) + frustum[p][2] * (z - size) + frustum[p][3] > 0)
                continue;
            if (frustum[p][0] * (x - size) + frustum[p][1] * (y - size) + frustum[p][2] * (z + size) + frustum[p][3] > 0)
                continue;
            if (frustum[p][0] * (x + size) + frustum[p][1] * (y - size) + frustum[p][2] * (z + size) + frustum[p][3] > 0)
                continue;
            if (frustum[p][0] * (x - size) + frustum[p][1] * (y + size) + frustum[p][2] * (z + size) + frustum[p][3] > 0)
                continue;
            if (frustum[p][0] * (x + size) + frustum[p][1] * (y + size) + frustum[p][2] * (z + size) + frustum[p][3] > 0)
                continue;
            return false;
       }
       
       return true;
    }
    
    public void setModelMatrix(Matrix4f mat)
    {
        model.load(mat);
        isUpdated = false;
    }
    /*public void setModelMatrix(
            float xPos, float yPos, float zPos,
            float xScale, float yScale, float zScale,
            float xRot, float yRot, float zRot)
    {
        model = modelMatrix(
                xPos, yPos, zPos,
                xScale, yScale, zScale,
                xRot, yRot, zRot);
        isUpdated = false;
    }*/
    public void setModelMatrix(Vector3f pos, Vector3f scale, Vector3f rot)
    {
        model.setIdentity();
        modelMatrix(pos, scale, rot, model);
        isUpdated = false;
    }
    
    private static final Vector3f
            p = new Vector3f(),
            s = new Vector3f(),
            r = new Vector3f();
    
    public void setModelMatrix(
            float xPos, float yPos, float zPos,
            float xScale, float yScale, float zScale,
            float xRot, float yRot, float zRot)
    {
        p.x = xPos;
        p.y = yPos;
        p.z = zPos;
        s.x = xScale;
        s.y = yScale;
        s.z = zScale;
        r.x = xRot;
        r.y = yRot;
        r.z = zRot;
        modelMatrix(p, s, r, model);
    }
    
    private static final Vector3f
            vx = new Vector3f(0, 0, 1),
            vy = new Vector3f(0, 1, 0),
            vz = new Vector3f(1, 0, 0);
    
    public static Matrix4f modelMatrix(Vector3f pos, Vector3f scale, Vector3f rot, Matrix4f dest)
    {
        if (dest == null)
        {
            dest = new Matrix4f();
        }
        else
        {
            dest.setIdentity();
        }
        
        Matrix4f.translate(pos, dest, dest);
        
        Matrix4f.scale(scale, dest, dest);
        
        Matrix4f.rotate(rot.z, vx,
                dest, dest);
        Matrix4f.rotate(rot.y, vy,
                dest, dest);
        Matrix4f.rotate(rot.x, vz,
                dest, dest);
        
        return dest;
    }
    
    public void uniform(int matrixLocation, Matrix4f mat)
    {
        mat.store(matrixBuffer);
        glUniformMatrix4(matrixLocation, false, matrixBuffer);
        matrixBuffer.flip();
    }
    
    public Matrix4f uniformMVP(int matrixLocation)
    {
        return uniformMVP(matrixLocation, false);
    }
    public Matrix4f uniformMVP(int matrixLocation, boolean texBias)
    {
        Matrix4f MVP = storeMVP(texBias);
        glUniformMatrix4(matrixLocation, false, matrixBuffer);
        return MVP;
    }
    
    public Matrix4f storeMVP()
    {
        return storeMVP(false);
    }
    public Matrix4f storeMVP(boolean texBias)
    {
        Matrix4f MVP = getMVP(texBias);
        MVP.store(matrixBuffer);
        matrixBuffer.flip();
        return MVP;
    }
    
    public Matrix4f getMVP()
    {
        return getMVP(false);
    }
    public Matrix4f getMVP(boolean texBias)
    {
        isUpdated = false;
        isBiasUpdated = false;
        
        if (!isUpdated)
        {
            isBiasUpdated = false;
            Matrix4f.mul(projection, view, MVP);
            Matrix4f.mul(MVP, model, MVP);
            isUpdated = true;
        }
        
        if (texBias)
        {
            if (!isBiasUpdated)
            {
                Matrix4f.mul(biasMatrix, MVP, biasMVP);
                isBiasUpdated = true;
            }
            
            return biasMVP;
        }
        
        return MVP;
    }
    
    public Matrix4f getProjection()
    {
        return projection;
    }
    
    public Matrix4f getView()
    {
        return view;
    }
    
    public Matrix4f getModel()
    {
        return model;
    }
    
    public FloatBuffer matrixBuffer()
    {
        return matrixBuffer;
    }
    
    private static Matrix4f biasMatrix()
    {
        Matrix4f mat = new Matrix4f();
        
        mat.m00 = .5f;
        mat.m11 = .5f;
        mat.m22 = .5f;
        mat.m30 = .5f;
        mat.m31 = .5f;
        mat.m32 = .5f;
        
        return mat;
    }
}
