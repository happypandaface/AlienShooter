package com.me.alienShooter;

import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;

/*
 * Written by Scott Griffy
 * The code in this class has only been tested on meshes (with applied rot+scale+pos) exported from blender to FBX then converted to G3Dj with fbx-conv
 * fbx-conv binaries can be found for windows on the page here:
 * http://libgdx.badlogicgames.com/fbx-conv/
 */

public class ExtendedGameUtil
{
	private Vector3 calcNormU = new Vector3();
	private Vector3 calcNormV = new Vector3();
	private Vector3 calcNormTemp = new Vector3();
	private Vector3 calcNormV2 = new Vector3(0, 1, 0).nor();
	public void calcNormal(Vector3[] closestIntersectionTriang, boolean flipZ, Vector3 returnV, Quaternion quat)
	{
		// get the normal vector from the triangle hit
		calcNormU = closestIntersectionTriang[1].cpy().sub(closestIntersectionTriang[0]);
		calcNormV = closestIntersectionTriang[2].cpy().sub(closestIntersectionTriang[0]);
		calcNormTemp.set(calcNormU.crs(calcNormV).nor());
		
		if (flipZ)
		{
			// it's probably inverted (depends on exporter)
			float tempNormal = calcNormTemp.y;
			calcNormTemp.y = calcNormTemp.z;
			calcNormTemp.z = -tempNormal;
		}
		
		// find quaternion from normal
		quat.setFromCross(calcNormV2, calcNormTemp);
		
		returnV.set(calcNormTemp);
	}
	public int getNumFloatTriangles(MeshPart meshPart)
	{
		return meshPart.numVertices*3;
	}
	public void getTriangles(MeshPart meshPart, float[] triags)
	{
		// get the mesh of the mesh part (this holds the vertices+normals+UV+otherstuff)
		Mesh mesh = meshPart.mesh;
		// this changes based on the what stuff the mesh has (vertices+normals+UV+idk)
		int floatsInAVertex = mesh.getVertexSize()/4;
		// each vertices will need enough floats for all the info including vertices+normals+UV+otherstuff
		float[] verts = new float[mesh.getNumVertices()*floatsInAVertex];
		mesh.getVertices(verts);
		// this is a list of all the indices in the mesh. Every 3 is a triangle also holds a lot of extra space for buffers
		short[] indicesFull = new short[mesh.getNumIndices()];
		mesh.getIndices(indicesFull);
		// need to get rid of the extra indices not used by this MeshPart
		short[] indices = new short[meshPart.numVertices];
		int currIndex = 0;
		for (int i = 0 ; i < indicesFull.length ; ++i)
		{
			// use only the indices in the mesh part's range
			if (i >= meshPart.indexOffset && i < meshPart.indexOffset+meshPart.numVertices)
			{
				indices[currIndex] = indicesFull[i];
				++currIndex;
			}
		}
		// now make the triangle array
		int indNum = 0;
		while (indNum < meshPart.numVertices)
		{
			triags[indNum*3] = verts[indices[indNum]*floatsInAVertex];
			triags[indNum*3+1] = verts[indices[indNum]*floatsInAVertex+1];
			triags[indNum*3+2] = verts[indices[indNum]*floatsInAVertex+2];
			indNum++;
		}
	}
	public void getNormals(MeshPart meshPart, float[] normals)
	{
		// get the mesh of the mesh part (this holds the vertices+normals+UV+otherstuff)
		Mesh mesh = meshPart.mesh;
		// this changes based on the what stuff the mesh has (vertices+normals+UV+idk)
		int floatsInAVertex = mesh.getVertexSize()/4;// sizeof(float)
		// each vertices will need enough floats for all the info including vertices+normals+UV+otherstuff
		float[] verts = new float[mesh.getNumVertices()*floatsInAVertex];
		mesh.getVertices(verts);
		// this is a list of all the indices in the mesh. Every 3 is a triangle also holds a lot of extra space for buffers
		short[] indicesFull = new short[mesh.getNumIndices()];
		mesh.getIndices(indicesFull);
		// need to get rid of the extra indices not used by this MeshPart
		short[] indices = new short[meshPart.numVertices];
		int currIndex = 0;
		for (int i = 0 ; i < indicesFull.length ; ++i)
		{
			// use only the indices in the mesh part's range
			if (i >= meshPart.indexOffset && i < meshPart.indexOffset+meshPart.numVertices)
			{
				indices[currIndex] = indicesFull[i];
				++currIndex;
			}
		}
		// now make the normal array
		int indNum = 0;
		while (indNum < meshPart.numVertices)
		{
			/* this is found slightly differently than the triangles array because normals are USUALLY just after position in the vertex information
			 * usually is capitalized because this only holds true for certain meshes and this code will break if the vertex information order is changed
			 * this means this code needs to be reworked to account for vertex information order
			 */
			normals[indNum*3] = verts[indices[indNum]*floatsInAVertex+3];
			normals[indNum*3+1] = verts[indices[indNum]*floatsInAVertex+4];
			normals[indNum*3+2] = verts[indices[indNum]*floatsInAVertex+5];
			indNum++;
		}
	}
	public void getModelInstanceArrTriangles(Array<ModelInstance> mis, ArrayMap<Model, Array<Vector3[]>> miARRtriangs)
	{
		Iterator<ModelInstance> miIterator = mis.iterator();
		while (miIterator.hasNext())
		{
			ModelInstance miTest = miIterator.next();
			Model m = miTest.model;
			if (miARRtriangs.get(m) == null)
			{
				Array<Vector3[]> testArrVec = new Array<Vector3[]>();
				getModelInstanceTriangles(miTest, testArrVec);
				miARRtriangs.put(m, testArrVec);
			}
		}
	}
	public void getModelArrTriangles(ArrayMap<String, Model> mdls, ArrayMap<Model, Array<Vector3[]>> miARRtriangs)
	{
		Iterator<Model> mdlIter = mdls.values();
		while (mdlIter.hasNext())
		{
			Model m = mdlIter.next();
			if (miARRtriangs.get(m) == null)
			{
				Array<Vector3[]> testArrVec = new Array<Vector3[]>();
				getModelInstanceTriangles(m, testArrVec);
				miARRtriangs.put(m, testArrVec);
			}
		}
	}
	public void getModelInstanceTriangles(ModelInstance mi, Array<Vector3[]> triangs)
	{
		Iterator<Node> nodeIter = mi.nodes.iterator();
		while (nodeIter.hasNext())
		{
			Node n = nodeIter.next();
			n.calculateLocalTransform();
			Iterator<NodePart> nodePartIter = n.parts.iterator();
			while (nodePartIter.hasNext())
			{
				NodePart np = nodePartIter.next();
				MeshPart meshPart = np.meshPart;
				float[] triangsF = new float[this.getNumFloatTriangles(meshPart)];
				this.getTriangles(meshPart, triangsF);
				//float[] normals = new float[this.getNumFloatTriangles(meshPart)];
				//this.getNormals(meshPart, normals);
				for (int i = 0; i < meshPart.numVertices/3; ++i)
				{
					Vector3[] triVectors = new Vector3[3];
					for (int c = 0; c < 3; ++c)
					{
						triVectors[c] = new Vector3(
								triangsF[i*9+c*3],// the 9 is the triangle size (3 vectors = 9 floats)
								triangsF[i*9+c*3+1],
								triangsF[i*9+c*3+2]);
					}
					triangs.add(triVectors);
				}
			}
		}
	}
	public void getModelInstanceTriangles(Model mi, Array<Vector3[]> triangs)
	{
		Iterator<Node> nodeIter = mi.nodes.iterator();
		while (nodeIter.hasNext())
		{
			Node n = nodeIter.next();
			n.calculateLocalTransform();
			Iterator<NodePart> nodePartIter = n.parts.iterator();
			while (nodePartIter.hasNext())
			{
				NodePart np = nodePartIter.next();
				MeshPart meshPart = np.meshPart;
				float[] triangsF = new float[this.getNumFloatTriangles(meshPart)];
				this.getTriangles(meshPart, triangsF);
				//float[] normals = new float[this.getNumFloatTriangles(meshPart)];
				//this.getNormals(meshPart, normals);
				for (int i = 0; i < meshPart.numVertices/3; ++i)
				{
					Vector3[] triVectors = new Vector3[3];
					for (int c = 0; c < 3; ++c)
					{
						triVectors[c] = new Vector3(
								triangsF[i*9+c*3],// the 9 is the triangle size (3 vectors = 9 floats)
								triangsF[i*9+c*3+1],
								triangsF[i*9+c*3+2]);
					}
					triangs.add(triVectors);
				}
			}
		}
	}
	private Vector3 testIntersect = new Vector3();
	private Vector3[] testTriags = new Vector3[3];
	public float getIntersectionTriangles(Array<ModelInstance> mis, ArrayMap<Model, Array<Vector3[]>> miARRtriangs, Ray pickRay, Vector3 intersection, Vector3[] triags, ModelInstance[] mi)
	{
		getModelInstanceArrTriangles(mis, miARRtriangs);
		float closestIntersectionDistance = -1;
		Iterator<ModelInstance> miIterator = mis.iterator();
		//Iterator<Array<Vector3[]>> miArrIter = miARRtriangs.iterator();
		while (miIterator.hasNext())
		{
			ModelInstance miTest = miIterator.next();
			float dist = getIntersectionTriangles(miTest, pickRay, miARRtriangs.get(miTest.model), testIntersect, testTriags);
			//Gdx.app.log("distTop", ""+dist);
			if (dist != -1 && (closestIntersectionDistance == -1 || dist < closestIntersectionDistance))
			{
				closestIntersectionDistance = dist;
				intersection.set(testIntersect);
				for (int i = 0; i < 3; i++)
				{
					triags[i] = testTriags[i];
				}
				mi[0] = miTest;
			}
		}
		return closestIntersectionDistance;
	}
	public float getIntersectionTriangles(Array<ModelInstance> mis, Ray pickRay, Vector3 intersection, Vector3[] triags, ModelInstance[] mi)
	{
		ArrayMap<Model, Array<Vector3[]>> miARRtriangs = new ArrayMap<Model, Array<Vector3[]>>();
		getModelInstanceArrTriangles(mis, miARRtriangs);
		return getIntersectionTriangles(mis, miARRtriangs, pickRay, intersection, triags, mi);
	}

	private Ray tempPickRay = new Ray(new Vector3(), new Vector3());
	public float getIntersectionTriangles(ModelInstance mi, Ray pickRay, Array<Vector3[]> miARRtriangs, Vector3 intersection, Vector3[] triags)
	{
		//Array<Vector3[]> ArrTriangs = miARRtriangs.get(mi.model);
		//getModelInstanceTriangles(mi, ArrTriangs);
		Vector3 intersect = new Vector3();
		float closestIntersectionDistance = -1;
		Iterator<Node> nodeIter = mi.nodes.iterator();
		Iterator<Vector3[]> triangArr = miARRtriangs.iterator();
		while (nodeIter.hasNext())
		{
			Node n = nodeIter.next();
			Iterator<NodePart> nodePartIter = n.parts.iterator();
			while (nodePartIter.hasNext())
			{
				NodePart np = nodePartIter.next();
				MeshPart meshPart = np.meshPart;
				//float[] triangs = triangArr.next();//new float[this.getNumFloatTriangles(meshPart)];
				//this.getTriangles(meshPart, triangs);
				//float[] normals = new float[this.getNumFloatTriangles(meshPart)];
				//this.getNormals(meshPart, normals);
				for (int i = 0; i < meshPart.numVertices/3; ++i)
				{
					Vector3[] triVectors = triangArr.next();//new Vector3[3];
					/*
					for (int c = 0; c < 3; ++c)
					{
						triVectors[c] = new Vector3(
								triangs[i*9+c*3],// the 9 is the triangle size (3 vectors = 9 floats)
								triangs[i*9+c*3+1],
								triangs[i*9+c*3+2]);
					}*/
					// normals may not be used
					/*
					Vector3[] triNormals = new Vector3[3];
					for (int c = 0; c < 3; ++c)
					{
						triNormals[c] = new Vector3(
								triangs[i*9+c*3],
								triangs[i*9+c*3+1],
								triangs[i*9+c*3+2]);
					}*/
					tempPickRay.set(pickRay);
					//n.calculateLocalTransform();// shouldnt be called on the render loop
					tempPickRay.mul(mi.transform.cpy().inv());
					tempPickRay.mul(n.globalTransform.cpy().inv());
					if (tempPickRay != null && 
							Intersector.intersectRayTriangle(
									tempPickRay, 
									triVectors[0], triVectors[1], triVectors[2], intersect))
					{
						/* the following code has been commented out for future debugging
						for (int c = 0; c < 3; ++c)
						{
							Gdx.app.log("normals", "normals"+triNormals[c].x+"y"+triNormals[c].y+"z"+triNormals[c].z);
						}
						*/
						float dist = intersect.dst(tempPickRay.origin);
						//Gdx.app.log("distBot", ""+dist);
						if (dist != -1 && (closestIntersectionDistance == -1 || dist < closestIntersectionDistance))
						{
							closestIntersectionDistance = dist;
							intersection.set(intersect.cpy());
							for (int c = 0; c < 3; ++c)
							{
								triags[c] = triVectors[c];
							}
						}
						
					}
				}
			}
		}
		return closestIntersectionDistance;
	}
	public void reorientIntersection(Vector3 intersection) 
	{
		// the intersection point needs to be flipped as well
		float temp = intersection.y;
		intersection.y = intersection.z;
		intersection.z = -temp;
	}
	public void setForBillBoard(Vector3 intersection, Vector3 nor) 
	{
		// this pushes the bullet out a bit from the wall
		intersection.add(nor.cpy().scl(.1f, .1f, .1f));
	}
}
