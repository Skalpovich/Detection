package application;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import Outils.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.opencv.videoio.VideoCapture;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Controller {
	private VideoCapture vc=new VideoCapture();
	private ScheduledExecutorService time;
	private boolean active = false;
	private int taille=0;
	Mat imageROI;
	private CascadeClassifier visage=new CascadeClassifier();
	@FXML
	protected ImageView view;
	public void init()
	{
		view.setFitWidth(600);
		view.setPreserveRatio(true);
		visage.load("resources/lbpcascades/lbpcascade_frontalface.xml");
		this.start();
	}
	
	private void start()
	{
		
		if(!this.active)
		{
			this.vc.open(0);
			
			if( this.vc.isOpened()) {
				this.active=true;
						Runnable r=new Runnable(){
							@Override
							public void run() {
								Mat m=new Mat();
								vc.read(m);
								if(!m.empty())
								{
		MatOfRect face = new MatOfRect();
		Mat gray = new Mat();
		Imgproc.cvtColor(m, gray, Imgproc.COLOR_BGR2GRAY);
		Imgproc.equalizeHist(gray, gray);
		if (taille == 0)
		{
			int height = gray.rows();
			if (Math.round(height * 0.4f) > 0)
			{
			taille = Math.round(height * 0.4f);
			}
		}
		//Detection
		visage.detectMultiScale(gray, face, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE,
		new Size(taille, taille), new Size());
					int j=0;	
					boolean a=true;
		Rect[] visages = face.toArray();
		
		for (int i = 0; i < visages.length; i++) {
		Imgproc.rectangle(m, visages[i].tl(), visages[i].br(), new Scalar(0, 255, 0), 3);							
		Rect visageDetecter = new Rect(visages[i].x, visages[i].y ,visages[i].width,visages[i].height);
		imageROI = m.submat(visageDetecter);
		File dir = new File("test/v"+j);
		dir.mkdir();
		Imgcodecs.imwrite("test/v"+j+"/"+i+".jpg", imageROI);
		
		}
		
		}			
					Image image = Utils.mat2Image(m);
					updateImageView(view, image);
					
				}

			

				

				
			};
			this.time = Executors.newSingleThreadScheduledExecutor();
			this.time.scheduleAtFixedRate(r, 0, 33, TimeUnit.MILLISECONDS);
			//this.b.setText("Stop");
		}
		}
		else {
			this.active=false;
			//this.b.setText("Start");
			this.stopCamera();
		}
		
	}
	
	private void updateImageView(ImageView view, Image image)
	{
		Utils.onFXThread(view.imageProperty(), image);
	}
	public void stopCamera()
	{
		if (this.time!=null && !this.time.isShutdown())
		{
			try
			{
				// stop the timer
				this.time.shutdown();
				this.time.awaitTermination(33, TimeUnit.MILLISECONDS);
			}
			catch (InterruptedException e)
			{
				// log any exception
				System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
			}
		}
		
		if (this.vc.isOpened())
		{
			this.vc.release();
		}
	}
	
}
