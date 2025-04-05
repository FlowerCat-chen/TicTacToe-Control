package com.flowercat.chessControl;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

public class DrawBoardView extends View {
    private Paint paint;
    private Path currentPath;
    private List<DrawingPath> paths = new ArrayList<>();
    private int currentColor = Color.BLACK;
    private RectF[] colorRects = new RectF[3];
    private RectF clearRect;
    private int buttonAreaHeight;

	//存储绘制的所有点
	private static class Point {
		int x;
		int y;

		Point(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}

	private List<List<Point>> allPoints = new ArrayList<>();
	private List<Point> currentPoints;
	
	
	
	
	
    public DrawBoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(8f);
        buttonAreaHeight = dpToPx(60);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setupButtons();
    }

    private void setupButtons() {
        float buttonWidth = getWidth() / 4f;
        float colorButtonTop = getHeight() - buttonAreaHeight;

        // 设置颜色按钮位置（黑、红、绿）
        for (int i = 0; i < 3; i++) {
            colorRects[i] = new RectF(
                i * buttonWidth,
                colorButtonTop,
                (i + 1) * buttonWidth,
                getHeight()
            );
        }

        // 设置清空按钮位置
        clearRect = new RectF(
            3 * buttonWidth,
            colorButtonTop,
            getWidth(),
            getHeight()
        );
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 绘制所有路径
        for (DrawingPath dp : paths) {
            paint.setColor(dp.color);
            canvas.drawPath(dp.path, paint);
        }

        // 绘制当前路径
        if (currentPath != null) {
            paint.setColor(currentColor);
            canvas.drawPath(currentPath, paint);
        }

        // 绘制按钮
        drawButtons(canvas);
		//绘制中心
		canvas.drawCircle(getWidth()/2,(getHeight()-dpToPx(60))/2,10,paint);
    }

    private void drawButtons(Canvas canvas) {
        Paint btnPaint = new Paint();
        // 绘制颜色按钮
        btnPaint.setColor(Color.BLACK);
        canvas.drawRect(colorRects[0], btnPaint);
        btnPaint.setColor(Color.RED);
        canvas.drawRect(colorRects[1], btnPaint);
        btnPaint.setColor(Color.GREEN);
        canvas.drawRect(colorRects[2], btnPaint);

        // 绘制清空按钮
        btnPaint.setColor(Color.GRAY);
        canvas.drawRect(clearRect, btnPaint);

        // 绘制清空文字
        Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(dpToPx(16));
        textPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("清空", clearRect.centerX(), 
						clearRect.centerY() + textPaint.getTextSize()/2, textPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
		
		getParent().requestDisallowInterceptTouchEvent(true);
        float x = event.getX();
        float y = event.getY();

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            // 检查按钮点击
            if (handleButtonClick(x, y)) {
                return true;
            }

            // 开始新路径
            currentPath = new Path();
            currentPath.moveTo(x, y);
			currentPoints = new ArrayList<>();
            currentPoints.add(new Point((int)x, (int)y));
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (currentPath != null && y < getHeight() - buttonAreaHeight) {
                currentPath.lineTo(x, y);
				currentPoints.add(new Point((int)x, (int)y));
            }
        }

        if (event.getAction() == MotionEvent.ACTION_UP && currentPath != null) {
            paths.add(new DrawingPath(currentPath, currentColor));
			allPoints.add(currentPoints);
			currentPoints = null;
            currentPath = null;
        }

        invalidate();
        return true;
    }

	
	
	public List<Point> scalePoints(List<Point> points, int newWidth, int newHeight) {
		int originalWidth = getWidth();
		int originalHeight = getHeight() - buttonAreaHeight;

		List<Point> scaled = new ArrayList<>();
		for (Point p : points) {
			int x = (int) (p.x * ((float) newWidth / originalWidth));
			int y = (int) (p.y * ((float) newHeight / originalHeight));
			scaled.add(new Point(x, y));
		}
		return scaled;
	}
	
	private List<Point> samplePoints(List<Point> points, int minDistance) {
		List<Point> sampled = new ArrayList<>();
		if (points.size() < 2) return points;

		// 总是包含起点
		sampled.add(points.get(0));
		Point lastPoint = points.get(0);
		float accumulatedDist = 0;

		for (int i = 1; i < points.size(); i++) {
			Point current = points.get(i);
			float dx = current.x - lastPoint.x;
			float dy = current.y - lastPoint.y;
			float segmentDist = (float) Math.sqrt(dx*dx + dy*dy);

			accumulatedDist += segmentDist;

			// 当累计距离超过阈值时采样
			if (accumulatedDist >= minDistance) {
				sampled.add(current);
				lastPoint = current;
				accumulatedDist = 0; // 重置累计
			}
		}

		// 确保包含终点
		if (!sampled.get(sampled.size()-1).equals(points.get(points.size()-1))) {
			sampled.add(points.get(points.size()-1));
		}

		return sampled;
	}
	
	
	private String buildBluetoothData(List<Point> points) {
		StringBuilder sb = new StringBuilder();
		int index = 1;
		for (Point p : points) {
			if (sb.length() > 0) sb.append("|");
			sb.append(index).append(":").append(p.x).append(",").append(p.y);
			index++;
		}
		return sb.toString();
	}
	
	
	
	public String getToSend(int targetWidth, int targetHeight) {
		List<Point> allProcessed = new ArrayList<>();

		if(allPoints.isEmpty()){
			return "空";
		}
		for (List<Point> path : allPoints) {
			// 坐标转换
			List<Point> scaled = scalePoints(path, targetWidth, targetHeight);

			// 差值处理（这里用步长5）
			List<Point> sampled = samplePoints(scaled, 30);

			allProcessed.addAll(sampled);
		}

		// 生成最终字符串
		String bluetoothData = buildBluetoothData(allProcessed);

		// 通过蓝牙发送bluetoothData
		return bluetoothData;
	}
	
	
	
    private boolean handleButtonClick(float x, float y) {
        // 检查颜色按钮
        for (int i = 0; i < 3; i++) {
            if (colorRects[i].contains(x, y)) {
                currentColor = i == 0 ? Color.BLACK : 
					i == 1 ? Color.RED : Color.GREEN;
                invalidate();
                return true;
            }
        }

        // 检查清空按钮
        if (clearRect.contains(x, y)) {
            paths.clear();
			if(allPoints != null){
				allPoints.clear();
			}
            invalidate();
            return true;
        }
        return false;
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private static class DrawingPath {
        Path path;
        int color;

        DrawingPath(Path path, int color) {
            this.path = path;
            this.color = color;
        }
    }
}
