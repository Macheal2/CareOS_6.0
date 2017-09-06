package com.cappu.halllockscreen.anim;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class CustomRotateAnimation
  extends Animation
{
  public static final int ANIMATE_DOWN_TO_UP = 3;
  public static final int ANIMATE_LEFT_TO_RIGHT = 0;
  public static final int ANIMATE_RIGHT_TO_LEFT = 1;
  public static final int ANIMATE_UP_TO_DOWN = 2;
  private Camera mCamera;
  private final float mCenterX;
  private final float mCenterY;
  private final float mDepthZ;
  private final float mFromDegrees;
  private final boolean mReverse;
  private boolean mRotateY = true;
  private final float mToDegrees;
  
  public CustomRotateAnimation(boolean paramBoolean1, float paramFloat1, float paramFloat2, float paramFloat3, float paramFloat4, float paramFloat5, boolean paramBoolean2)
  {
    this.mRotateY = paramBoolean1;
    this.mFromDegrees = paramFloat1;
    this.mToDegrees = paramFloat2;
    this.mCenterX = paramFloat3;
    this.mCenterY = paramFloat4;
    this.mDepthZ = paramFloat5;
    this.mReverse = paramBoolean2;
  }
  
	protected void applyTransformation(float paramFloat,
			Transformation paramTransformation) {
		float f1 = this.mFromDegrees;
		float f2 = f1 + paramFloat * (this.mToDegrees - f1);
		float f3 = this.mCenterX;
		float f4 = this.mCenterY;
		Camera localCamera = this.mCamera;
		Matrix localMatrix = paramTransformation.getMatrix();
		localCamera.save();
		if (this.mReverse) {
			localCamera.translate(0.0F, 0.0F, paramFloat * this.mDepthZ);
		} else {
			localCamera.translate(0.0F, 0.0F, this.mDepthZ
					* (1.0F - paramFloat));
		}
		if(this.mRotateY){
			localCamera.rotateY(f2);
		}else{
			localCamera.rotateX(f2);
		}
		localCamera.getMatrix(localMatrix);
		localCamera.restore();
		localMatrix.preTranslate(-f3, -f4);
		localMatrix.postTranslate(f3, f4);
	}
  
  public boolean currentRotateY()
  {
    return this.mRotateY;
  }
  
  public float getNewEndAngle()
  {
    return 2.0F * this.mFromDegrees;
  }
  
  public float getOldEndAngle()
  {
    return this.mToDegrees;
  }
  
  public void initialize(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    super.initialize(paramInt1, paramInt2, paramInt3, paramInt4);
    this.mCamera = new Camera();
  }
}

