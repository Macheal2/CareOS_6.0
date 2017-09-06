
package com.cappu.launcherwin;


import java.util.HashMap;

import com.cappu.launcherwin.tools.DensityUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;


public class DragController {
    private String TAG = "DragController";
    
    /**
     * Mode_Free = 0
     *  @para 三种滑动状态，默认为静止状态  0 静止状态    1  当前页面下，拖动状态    2 跨页面滚动状态*/
    public static int Mode_Free = 0; // 静止状态
    /**
     *  Mode_Drag = 1; 
     * @para 三种滑动状态，默认为静止状态  0 静止状态    1  当前页面下，拖动状态    2 跨页面滚动状态*/
    public static int Mode_Drag = 1; // 当前页面下，拖动状态
    /**
     * Mode_Scroll = 2
     *  @para  三种滑动状态，默认为静止状态  0 静止状态    1  当前页面下，拖动状态    2 跨页面滚动状态*/
    public static int Mode_Scroll = 2; // 跨页面滚动状态
    
    private int Mode = Mode_Free;
    
    /**上次位移滑动到的X坐标位置*/
    private float mLastMotionX;
    /**上次位移滑动到的Y坐标位置*/
    private float mLastMotionY;
    
    /**拖动点的X坐标（加上当前屏数 * screenWidth）*/
    private int dragPointX;
    /**拖动点的Y坐标*/
    private int dragPointY;
    /**X坐标偏移量*/
    private int dragOffsetX;
    /**Y坐标偏移量*/
    private int dragOffsetY;
    
    /**手势落下的X坐标*/
    private int startX = 0;
    
    /**拖拽点的位置编号，每个Item对应一个位置编号，自增*/
    private int dragPosition = -1;

    /**临时交换位置的编号*/
    private int temChangPosition = -1;
    
    /**左边距*/
    private int leftPadding = 0;
    /**右边距*/
    private int rightPadding = 0;
    /**上边距*/
    private int topPadding = 0;
    /**下边距*/
    private int bottomPadding = 0;
    
    /**行间距*/
    private int rowSpace = 0;
    /**列间距*/
    private int colSpace = 0;
    
    /**item的宽度*/
    private int childWidth = 0;
    /**item的高度*/
    private int childHeight = 0;
    
    /**手机屏幕宽度*/
    private int screenWidth = 0;
    /**手机屏幕高度*/
    private int screenHeight = 0;
    
    /**总Item数*/
    private int totalItem = 0;
    /**总页数*/
    private int totalPage = 0;
    
    /**动态设置行数*/
    private int rowCount = 3;
    /**动态设置列数*/
    private int colCount = 2;
    /**每一页的Item总数*/
    private int itemPerPage = 6;
    
    /**每个Item图片宽度的半长，用于松下手指时的动画*/
    private int halfBitmapWidth;
    /**同上*/
    private int halfBitmapHeight;
    
    /**拖拽Item的子View*/
    private ImageView dragImageView;
    /**拖拽View对应的位图*/
    private Bitmap dragBitmap;
    
    /**window管理器，负责随手势显示拖拽View*/
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams windowParams;
    
    /**系列动画执行完成标识的集合*/
    private HashMap<Integer, Boolean> animationMap = new HashMap<Integer, Boolean>();
    
    Launcher mLauncher;
    
    Workspace mWorkspace;
    /**当前屏数*/
    private int mCurScreen;
    
    /**用来判断滑动到哪一个item的位置*/
    private Rect frame;
    
    public DragController(Launcher launcher) {
        this.mLauncher = launcher;
    }
    
    /**开始拖动*/
    public void startDrag(Bitmap bm, int x, int y, View itemView) {
        Log.i(TAG, "进入startDrag");
        dragPointX = x - itemView.getLeft() + mCurScreen * screenWidth;
        dragPointY = y - itemView.getTop();
        windowParams = new WindowManager.LayoutParams();

        windowParams.gravity = Gravity.TOP | Gravity.LEFT;
        windowParams.x = x - dragPointX + dragOffsetX;
        windowParams.y = y - dragPointY + dragOffsetY;
        windowParams.height = LayoutParams.WRAP_CONTENT;
        windowParams.width = LayoutParams.WRAP_CONTENT;
        windowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

        windowParams.format = PixelFormat.TRANSLUCENT;
        windowParams.windowAnimations = 0;
        windowParams.alpha = 0.8f;

        ImageView iv = new ImageView(mLauncher);
        iv.setImageBitmap(bm);
        dragBitmap = bm;
        mWindowManager = (WindowManager) mLauncher.getSystemService(Context.WINDOW_SERVICE);
        mWindowManager.addView(iv, windowParams);
        dragImageView = iv;
        Mode = Mode_Drag;

        halfBitmapWidth = bm.getWidth() / 2;
        halfBitmapHeight = bm.getHeight() / 2;

       /* for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).getBackground().setAlpha((int) (0.8f * 255));
        }*/
        Log.i(TAG, "进入startDrag end");
        
    }
    
    public void setWorkspace(Workspace workspace){
        if(mWorkspace == null){
            this.mWorkspace = workspace;
        }
        
        if(mWorkspace != null){
            mCurScreen = mWorkspace.getCurScreen();
            
            CellLayout cell = (CellLayout) mWorkspace.getChildAt(0);
            childWidth  = cell.getChildAt(0).getMeasuredWidth();
            childHeight = cell.getChildAt(0).getMeasuredHeight();
        }
    }
    
    public boolean dispatchTouchEvent(MotionEvent ev) {

        if(mWorkspace == null){
            return true;
        }
        //Log.i(TAG, "进入dispatchTouchEvent");
        final int action = ev.getAction();
        final float x = ev.getX();
        final float y = ev.getY();
        int thresholdX = DensityUtil.dip2px(mLauncher, 8);
        switch (action) {
		case MotionEvent.ACTION_DOWN:
			startX = (int) x;
			temChangPosition = dragPosition = pointToPosition((int) x, (int) y);
			dragOffsetX = (int) (ev.getRawX() - x);
			dragOffsetY = (int) (ev.getRawY() - y);
			mLastMotionX = x;
			mLastMotionY = y;
			startX = (int) x;
			totalPage = mWorkspace.getChildCount();
            break;
        case MotionEvent.ACTION_MOVE:
            int deltaX = (int) (mLastMotionX - x);

            if (IsCanMove(deltaX) && Math.abs(deltaX) > thresholdX && Mode != Mode_Drag) {
                mLastMotionX = x;
               // scrollBy(deltaX, 0);
                Mode = Mode_Scroll;
            }

            if (Mode == Mode_Drag) {
                onDrag((int) x, (int) y);
            }
            break;
        case MotionEvent.ACTION_CANCEL:
        case MotionEvent.ACTION_UP:
            float distance = ev.getRawX() - startX;
            if (distance > screenWidth / 6 && mCurScreen > 0 && Mode != Mode_Drag) {
                //snapToScreen(mCurScreen - 1);
            } else if (distance < -screenWidth / 6 && mCurScreen < totalPage - 1 && Mode != Mode_Drag) {
                //snapToScreen(mCurScreen + 1);
            } else if (Mode != Mode_Drag) {
                //snapToDestination();
            }
            if (Mode == Mode_Drag) {
                stopDrag();
            }
            if (dragImageView != null) {
                animationMap.clear();
                showDropAnimation((int) x, (int) y);
            }
            startX = 0;
            
            //showEdit(false);
            break;
        }
        return true;
    
    }
    
    /**根据手势绘制不断变化位置的dragView*/
    private void onDrag(int x, int y) {
        if (dragImageView != null) {
            windowParams.alpha = 0.8f;
            windowParams.x = (mCurScreen * screenWidth + x) - dragPointX + dragOffsetX;
            windowParams.y = y - dragPointY + dragOffsetY;
            //Log.i(TAG, "windowParams.:"+windowParams.x+"  "+windowParams.y+"    手指位置:"+x+"    "+y);
            mWindowManager.updateViewLayout(dragImageView, windowParams);
        }
        int tempPosition = pointToPosition(x, y);//手指滑动到某个位置，查找这个位置是否由磁块，如果找到磁块将 磁块位置保存在零时变量 tempPosition 里
        if (tempPosition != -1) {
            dragPosition = tempPosition;
        }
        View view = getIndexChildAt(temChangPosition);//getChildAt(temChangPosition);
        if (view == null) {
            stopDrag();
            return;
        }
        view.setVisibility(View.INVISIBLE);
        if (temChangPosition != dragPosition) {
            //Log.i(TAG, "零时点 temChangPosition:"+temChangPosition+"    dragPosition:"+dragPosition);
            
            View dragView = getIndexChildAt(temChangPosition);//getChildAt(temChangPosition);                                                         将拖动的view零时保存起来 (后面将放到 dragPosition 上去)
            movePostionAnimation(temChangPosition, dragPosition);//                                                                                                        执行两个view交换位置的动画
            
            indexRemoveViewAt(temChangPosition);//removeViewAt(temChangPosition);
            
            indexAddView(dragView,dragPosition);//addView(dragView, dragPosition);                                                                               将拖动的 view 放到 dragPosition 位置上去
            
            getIndexChildAt(dragPosition).setVisibility(View.INVISIBLE);//getChildAt(dragPosition).setVisibility(View.INVISIBLE);
            //this.getSaAdapter().exchange(temChangPosition, dragPosition);
            temChangPosition = dragPosition;
            
            Log.i(TAG, "--------------------------- 位置交换结束 -----------------------------------------");
        }

        if (x > mWorkspace.getRight() - DensityUtil.dip2px(mLauncher, 25) && mCurScreen < totalPage - 1 && mWorkspace.getScroller().isFinished() && x - startX > 10) {
            mWorkspace.snapToPage(mCurScreen + 1);//snapToScreen(mCurScreen + 1, false);
        } else if (x - mWorkspace.getLeft() < DensityUtil.dip2px(mLauncher, 35) && mCurScreen > 0 && mWorkspace.getScroller().isFinished() && x - startX < -10) {
            mWorkspace.snapToPage(mCurScreen - 1);//snapToScreen(mCurScreen - 1, false);
        }
    }
    
 // 停止拖动
	private void stopDrag() {
		if (Mode == Mode_Drag) {
			if (getIndexChildAt(dragPosition).getVisibility() != View.VISIBLE) {
				getIndexChildAt(dragPosition).setVisibility(View.VISIBLE);
			}
			Mode = Mode_Free;
		}
	}
    
    // 执行松手动画
    private void showDropAnimation(int x, int y) {
        ViewGroup moveView = (ViewGroup) getIndexChildAt(dragPosition);//getChildAt(dragPosition);
        TranslateAnimation animation = new TranslateAnimation(x - halfBitmapWidth - moveView.getLeft(), 0, y - halfBitmapHeight - moveView.getTop(),
                0);
        animation.setFillAfter(false);
        animation.setDuration(300);
        moveView.setAnimation(animation);
        mWindowManager.removeView(dragImageView);
        dragImageView = null;

        if (dragBitmap != null) {
            dragBitmap = null;
        }

        CellLayout cellLayout = getCurrentScreenView();
        for (int i = 0; i < cellLayout.getChildCount(); i++) {
            cellLayout.getChildAt(i).clearAnimation();
        }
    }
    
    /**根据坐标，判断当前item所属的位置，即编号*/
    public int pointToPosition(int x, int y) {
        if (frame == null){
            frame = new Rect();
        }
        CellLayout cellLayout =  (CellLayout) mWorkspace.getChildAt(mCurScreen);
        final int count = cellLayout.getChildCount();//mWorkspace
        for (int i =0;i<count;i++) {
            final BubbleView child = (BubbleView) cellLayout.getChildAt(i);
            child.getHitRect(frame);
            if (frame.contains(x, y)) {
                Log.i(TAG, "找到当前bubbleview所属的位置  :"+child.mTextView.getText().toString()+"所在位置 是:"+i+"    零时的位置temChangPosition:"+temChangPosition);
                return i;
            }
        }
        return -1;
      }
    
    /**获取 当前屏 cellLayout 的 BubbleView 对象*/
    public View getIndexChildAt(int position){
        CellLayout cellLayout =  (CellLayout) mWorkspace.getChildAt(mCurScreen);
        return cellLayout.getChildAt(position);
    }
    
    public CellLayout getCurrentScreenView(){
        CellLayout cellLayout =  (CellLayout) mWorkspace.getChildAt(mCurScreen);
        return cellLayout;
    }
    public void indexRemoveViewAt(int position){
        CellLayout cellLayout =  (CellLayout) mWorkspace.getChildAt(mCurScreen);
        cellLayout.removeViewAt(position);
    }
    
    public void indexRemoveView(View view){
        CellLayout cellLayout =  (CellLayout) mWorkspace.getChildAt(mCurScreen);
        cellLayout.removeView(view);
    }
    
    public void indexAddView(View view,int position){
        CellLayout cellLayout =  (CellLayout) mWorkspace.getChildAt(mCurScreen);
        cellLayout.addView(view, position);
    }
    
    /**执行位置动画*/
    private void movePostionAnimation(int oldP, int newP) {
        int moveNum = newP - oldP;
        boolean isCrossScreen = false;//是否跨屏
        boolean isForward = false;
        if (moveNum != 0 && !isMovingFastConflict(moveNum)) {
            int absMoveNum = Math.abs(moveNum);
            
            for (int i = Math.min(oldP, newP) + 1; i <= Math.max(oldP, newP); i++) {
                if (i % 6 == 0) {//这里的 6是当前屏所有item数
                    isCrossScreen = true;
                }
            }
            if (isCrossScreen) {
                isForward = moveNum < 0 ? false : true;
            }
            for (int i = 0; i < absMoveNum; i++) {
                int holdPosition = (moveNum > 0) ? oldP + 1 : oldP - 1; //moveNum > 0 是 低序号位置 view  向高序号的view移动       holdPosition 表示将原来的位置移动到新位置 的标号
                View view =  getIndexChildAt(holdPosition);//getChildAt(holdPosition);
                Log.i(TAG, "absMoveNum:"+absMoveNum+"    view:"+(view !=null));
                if (view != null) {
                    view.startAnimation(animationPositionToPosition(oldP, holdPosition, isCrossScreen, isForward));
                }
                indexRemoveView(view);
                indexAddView(view,holdPosition);
                
                oldP = holdPosition;
            }
        }
    }
    /**判断滑动的一系列动画是否有冲突*/
    private boolean isMovingFastConflict(int moveNum) {
        int itemsMoveNum = Math.abs(moveNum);
        int temp = dragPosition;
        for (int i = 0; i < itemsMoveNum; i++) {
            int holdPosition = moveNum > 0 ? temp + 1 : temp - 1;
            if (animationMap.containsKey(holdPosition)) {
                return true;
            }
            temp = holdPosition;
        }
        return false;
    }
    
    /**返回滑动的位移动画，比较复杂，有兴趣的可以看看*/
    private Animation animationPositionToPosition(int oldP, int newP, boolean isCrossScreen, boolean isForward) {
        PointF oldPF = positionToPoint2(oldP);
        PointF newPF = positionToPoint2(newP);

        TranslateAnimation animation = null;

        // when moving forward across pages,the first item of the new page moves
        // backward
        if (oldP != 0 && (oldP + 1) % itemPerPage == 0 && isForward) {
            animation = new TranslateAnimation(screenWidth - oldPF.x, 0, DensityUtil.dip2px(mLauncher, 25) - screenHeight, 0);
            animation.setDuration(800);
        } else if (oldP != 0 && oldP % itemPerPage == 0 && isCrossScreen && !isForward) { // when moving backward across pages,the last item of the new page moves forward
            animation = new TranslateAnimation(newPF.x - screenWidth, 0, screenHeight - DensityUtil.dip2px(mLauncher, 25), 0);
            animation.setDuration(800);
        }else {// regular animation between two neighbor items
            animation = new TranslateAnimation(newPF.x - oldPF.x, 0, newPF.y - oldPF.y, 0);
            animation.setDuration(500);
        }
        animation.setFillAfter(true);
        animation.setAnimationListener(new NotifyDataSetListener(oldP));

        return animation;
    }
    
 // item编号对应的左上角坐标
    public PointF positionToPoint1(int position) {
        PointF point = new PointF();

        int page = position / itemPerPage;
        int row = position / colCount % rowCount;
        int col = position % colCount;
        int left = leftPadding + page * screenWidth + col * (colSpace + childWidth);
        int top = topPadding + row * (rowSpace + childHeight);

        point.x = left;
        point.y = top;
        return point;
    }

    public PointF positionToPoint2(int position) {
        PointF point = new PointF();

        int row = position / colCount % rowCount;
        int col = position % colCount;
        int left = leftPadding + col * (colSpace + childWidth);
        int top = topPadding + row * (rowSpace + childHeight);

        point.x = left;
        point.y = top;
        return point;

    }
    
    /**滑动合法性的判断，防止滑动到空白区域*/
    private boolean IsCanMove(int deltaX) {
        if (mWorkspace.getScrollX() <= 0 && deltaX < 0) {
            return false;
        }
        if (mWorkspace.getScrollX() >= (totalPage - 1) * screenWidth && deltaX > 0) {
            return false;
        }
        return true;
    }
    
    public float getLastMotionX(){
        return mLastMotionX;
    }
    public float getLastMotionY(){
        return mLastMotionY;
    }
    
    public int getDragMode(){
        return Mode;
    }
    
    public void setScreenWidth(int sw){
        this.screenWidth = sw;
    }
    
    public void setScreenHeight(int sh){
        this.screenHeight = sh;
    }
    
    /**使用Map集合记录，防止动画执行混乱*/
    private class NotifyDataSetListener implements AnimationListener {
        private int movedPosition;

        public NotifyDataSetListener(int primaryPosition) {
            this.movedPosition = primaryPosition;
            
            Log.i(TAG, "NotifyDataSetListener:"+movedPosition);
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            
            Log.i(TAG, "--------------------------------    动画  +++++ 结束--------------------------------------------");
            
            if (animationMap.containsKey(movedPosition)) {
                // remove from map when end
                animationMap.remove(movedPosition);
            }
            
            
            Log.i(TAG, "动画执行完以后的位子movedPosition:"+movedPosition);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
            
            Log.i(TAG, "--------------------------------动画  ----- Repeat--------------------------------------------");
        }

        @Override
        public void onAnimationStart(Animation animation) {
            // put into map when start
            animationMap.put(movedPosition, true);
            
            Log.i(TAG, "--------------------------------动画开始--------------------------------------------");
        }
        
        
    }
}
