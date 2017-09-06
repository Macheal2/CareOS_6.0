
package com.cappu.launcherwin;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.MeasureSpec;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import com.cappu.launcherwin.basic.theme.ThemeManager;
import com.cappu.launcherwin.tools.DensityUtil;
import com.cappu.launcherwin.widget.LauncherLog;

public class CellLayout extends ViewGroup{
	private static final String TAG="CellLayout";//hejianfeng add 
//    public ArrayList<BubbleView> mList= new ArrayList<BubbleView>();
    
    private int mCellPadding = -1;
    
    private int padding=-1;
    public CellLayout(Context context) {
        this(context, null);
        if(ThemeManager.getInstance().getCurrentThemeType(getContext()) == ThemeManager.THEME_CHINESESTYLE){
            mCellPadding = 0;
        }else if(ThemeManager.getInstance().getCurrentThemeType() == ThemeManager.THEME_NINE_GRIDS){
        	mCellPadding = 0;
        }else{
            mCellPadding = context.getResources().getInteger(R.integer.cell_padding)+3;
        }
        //hejianfeng add start
        padding=DensityUtil.dip2px(getContext(), mCellPadding);
        //hejianfeng add end
    }

    public CellLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    @Override
    public void addView(View child, android.view.ViewGroup.LayoutParams params) {
        //super.addView(child, params);
        int index = -1;
        if(params instanceof  CellLayout.LayoutParams){
            CellLayout.LayoutParams layoutParams = ( CellLayout.LayoutParams)  params;
            index = layoutParams.cellY * 2 + layoutParams.cellX;
        }
        if(index == -1){
            super.addView(child,  params);
        }else{
            if(index >= getChildCount()){
                index = getChildCount();
            }
            super.addView(child, index, params);
        }
    }
    
    @Override
    public void addView(View child) {
        // TODO Auto-generated method stub
        super.addView(child);
    } 
    
    /**这里是拖拽执行的添加view，在这里将要对 LayoutParams 中的cellY  cellX 的值进行转换*/
    @Override
    public void addView(View child, int index) {
        
        LayoutParams params = (LayoutParams) child.getLayoutParams();
        if (params == null) {
            params = (LayoutParams) super.generateDefaultLayoutParams();
            if (params == null) {
                throw new IllegalArgumentException("generateDefaultLayoutParams() cannot return null");
            }
        }else{
                params.cellX = index%2;
                params.cellY = index/2;
        }
        Log.i("DragController", "将这个view 添加到:"+index+"    位置:"+"     params.cellX:"+params.cellX+"    params.cellY:"+params.cellY);
        super.addView(child, index, params);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();
            int childLeft = lp.x;
            int childTop = lp.y;
            
            int left =   childLeft;
            int top =    childTop;
            int right =  childLeft + lp.width;
            int bottom = childTop + lp.height;
            
            
            if(lp.cellY == 0){
                child.layout(left, top, right, bottom);
            }else{
                child.layout(left, top, right, bottom);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
//        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
//        if (widthMode != MeasureSpec.EXACTLY) {
//            throw new IllegalStateException( "ScrollLayout only canmCurScreen run at EXACTLY mode!");
//        }
//
//        if (heightMode != MeasureSpec.EXACTLY) {
//            throw new IllegalStateException("ScrollLayout only can run at EXACTLY mode!");
//        }
        //width 当前的celllayout 的宽度
        int width = MeasureSpec.getSize(widthMeasureSpec);
        //heigh 当前的celllayout 的高度
        int heigh = MeasureSpec.getSize(heightMeasureSpec)-padding;
      
        int cellHeight=heigh/3;
        //hejianfeng add start for 4x3
        int cellWidth = width/2;
        if(ThemeManager.getInstance().getCurrentThemeType() == ThemeManager.THEME_NINE_GRIDS){
        	cellWidth=width/3;
        	cellHeight = heigh/3;
        }
        //hejianfeng add end for 4x3
        int count = getChildCount();

        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            /** 竖屏情况 */
			/**
			 * cellWidth 磁块的宽度 cellHeight 磁块的高度 widthGap 间隔宽 heightGap 间隔高
			 * hStartPadding 水平 启动填充 边距 vStartPadding 垂直 启动填充 边距
			 * */
			lp.setup(cellWidth - padding, cellHeight - padding, padding,
					padding, padding / 2, padding / 2);

            if (lp.regenerateId) {
                child.setId(((getId() & 0xFF) << 16) | (lp.cellX & 0xFF) << 8 | (lp.cellY & 0xFF));
                lp.regenerateId = false;
            }

            int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY);
            int childheightMeasureSpec = MeasureSpec.makeMeasureSpec(lp.height, MeasureSpec.EXACTLY);
            child.measure(childWidthMeasureSpec, childheightMeasureSpec);
        }
        setMeasuredDimension(width, heigh);
    }
    
    /**下面这三个方法一定得写上 不然会 报LayoutParams 错误*/
    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new CellLayout.LayoutParams(getContext(), attrs);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof CellLayout.LayoutParams;
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new CellLayout.LayoutParams(p);
    }
    
    public static class LayoutParams extends ViewGroup.MarginLayoutParams {
        /**
         * Horizontal location of the item in the grid.
         * 在网格中的磁块的列数
         */
        public int cellX;

        /**
         * Vertical location of the item in the grid.
         * 在网格中的磁块的行数数
         */
        public int cellY;

        /**
         * Number of cells spanned horizontally by the item.
         * 磁块水平跨越的列数
         */
        public int cellHSpan;

        /**
         * Number of cells spanned vertically by the item.
         * 磁块垂直跨越的行数
         */
        public int cellVSpan;
        /**
         * Workspace page
         */
        public int screen;

        /**
         * X coordinate of the view in the layout.
         *在布局视图的X坐标
         **/
        int x;
        
        /**
          * Y coordinate of the view in the layout.
          *在布局视图的X坐标
          **/
        int y;
        
        /**重新生成一个ID*/
        boolean regenerateId;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            cellHSpan = 1;
            cellVSpan = 1;
            regenerateId = true;
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
            cellHSpan = 1;
            cellVSpan = 1;
        }
        
        public LayoutParams(int cellX, int cellY, int cellHSpan, int cellVSpan,int screen) {
            super(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            this.cellX = cellX;
            this.cellY = cellY;
            this.cellHSpan = cellHSpan;
            this.cellVSpan = cellVSpan;
            this.screen=screen;
        }

        /**
         * cellWidth     磁块的宽度
         * cellHeight    磁块的高度
         * widthGap      间隔宽
         * heightGap     间隔高
         * hStartPadding 水平 启动填充 边距
         * vStartPadding 垂直 启动填充 边距
         * */
        public void setup(int cellWidth, int cellHeight, int widthGap, int heightGap, int hStartPadding, int vStartPadding) {

            final int myCellHSpan = cellHSpan;
            final int myCellVSpan = cellVSpan;
            final int myCellX = cellX;
            final int myCellY = cellY;

            if (widthGap < 0) {
                width = myCellHSpan * cellWidth + (myCellHSpan * widthGap) - leftMargin - rightMargin;
            } else {
                width = myCellHSpan * cellWidth + ((myCellHSpan - 1) * widthGap) - leftMargin - rightMargin;
            }

            if (heightGap < 0) {
                height = myCellVSpan * cellHeight + (myCellVSpan * heightGap) - topMargin - bottomMargin;
            } else {
                height = myCellVSpan * cellHeight + ((myCellVSpan - 1) * heightGap) - topMargin - bottomMargin;
            }

            x = hStartPadding + myCellX * (cellWidth + widthGap) + leftMargin;
            y = vStartPadding + myCellY * (cellHeight + heightGap) + topMargin;
        }

        @Override
        public String toString() {
            return "LayoutParams [cellX=" + cellX + ", screen=" + screen + ", cellY=" + cellY + ", cellHSpan=" + cellHSpan + ", cellVSpan=" + cellVSpan + ", x=" + x
                    + ", y=" + y + ", regenerateId=" + regenerateId + "]";
        }
    }
    
    private Matrix drawMatrix;
    private boolean EditStatus;
    public int countDraw;
    public void setDrawMatrix(Matrix matrix,boolean EditStatus,int countDraw){
        this.drawMatrix = matrix;
        this.EditStatus = EditStatus;
        this.countDraw = countDraw;
    }
    
    public void postDraw(){
        invalidate();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);
        Log.i("HHJ", "onDraw :");
    }
    
    @Override
    public void dispatchDraw(Canvas canvas) {
        if(drawMatrix!=null){
            canvas.concat(drawMatrix);
            if(EditStatus){
                canvas.translate(countDraw*(24/4), countDraw*(24/4));
            }else{
                canvas.translate(24 - countDraw*(24/4), 24 - countDraw*(24/4));
            }
            
        }
        super.dispatchDraw(canvas);
    }
    @Override
    public void draw(Canvas canvas) {
        
        
        super.draw(canvas);
    }
    
}


