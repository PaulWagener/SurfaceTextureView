package nl.paulwagener;

import android.opengl.GLSurfaceView;
import android.view.View;

public interface GLView {
	public void queueEvent(Runnable runnable);
	public void setRenderer(GLSurfaceView.Renderer renderer);
	public View getView();
}
