package com.me.alienShooter;

import com.badlogic.gdx.math.Vector2;

public class MoveableUtil
{
	private static Vector2 temp1 = new Vector2();
	private static Vector2 temp2 = new Vector2();
	public static Vector2 getLogDiff(Moveable m1, Moveable m2, Vector2 tileDim, Vector2 rtn)
	{
		m1.getLogicalPos(temp1, tileDim);
		m2.getLogicalPos(temp2, tileDim);
		if (rtn == null)
			rtn = new Vector2();
		rtn.set(temp1.sub(temp2));
		return rtn;
	}
}
