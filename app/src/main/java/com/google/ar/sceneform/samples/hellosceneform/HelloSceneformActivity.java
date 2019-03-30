/*
 * Copyright 2018 Google LLC. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.ar.sceneform.samples.hellosceneform;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.Toast;
import com.google.ar.core.Anchor;
import com.google.ar.core.Camera;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.List;

/**
 * This is an example activity that uses the Sceneform UX package to make common AR tasks easier.
 */
public class HelloSceneformActivity extends AppCompatActivity {
  private static final String TAG = HelloSceneformActivity.class.getSimpleName();
  private static final double MIN_OPENGL_VERSION = 3.0;

  private ArFragment arFragment;
  private ModelRenderable andyRenderable;
  private AnchorNode mAnchorNode;
  private TransformableNode mTransformNode;
  private boolean isPlaced = false;
  private boolean isDone = false;
  float offset = 0.01f;

  @Override
  @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
  // CompletableFuture requires api level 24
  // FutureReturnValueIgnored is not valid
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (!checkIsSupportedDeviceOrFinish(this)) {
      return;
    }

    setContentView(R.layout.activity_ux);
    arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);

    // When you build a Renderable, Sceneform loads its resources in the background while returning
    // a CompletableFuture. Call thenAccept(), handle(), or check isDone() before calling get().
    ModelRenderable.builder()
        .setSource(this, R.raw.andy)
        .build()
        .thenAccept(renderable -> andyRenderable = renderable)
        .exceptionally(
            throwable -> {
              Toast toast =
                  Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG);
              toast.setGravity(Gravity.CENTER, 0, 0);
              toast.show();
              return null;
            });

    arFragment.getArSceneView().getScene().addOnUpdateListener(this.updateListener);


//    arFragment.setOnTapArPlaneListener(
//        (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
//          if (andyRenderable == null) {
//            return;
//          }
//
//          // Create the Anchor.
//          Anchor anchor = hitResult.createAnchor();
//          AnchorNode anchorNode = new AnchorNode(anchor);
//          anchorNode.setParent(arFragment.getArSceneView().getScene());
//
//          // Create the transformable andy and add it to the anchor.
//          TransformableNode andy = new TransformableNode(arFragment.getTransformationSystem());
//          andy.setParent(anchorNode);
//          andy.setRenderable(andyRenderable);
//          andy.select();
//        });
  }

  public Scene.OnUpdateListener updateListener = frameTime -> {
    Frame frame = arFragment.getArSceneView().getArFrame();

    if (frame == null) {
      return;
    }

    if (!this.isPlaced) {

      for (Plane plane : frame.getUpdatedTrackables(Plane.class)) {
        Point center = this.getScreenCenter();

        arFragment.getArSceneView().getScene().removeOnUpdateListener(this.updateListener);

        if (frame != null) {
          List<HitResult> result = frame.hitTest(center.x, center.y);

          for (HitResult hit : result) {
            Trackable trackable = hit.getTrackable();

            if (trackable instanceof Plane && ((Plane) trackable).isPoseInPolygon(hit.getHitPose())) {
              TransformableNode andy = new TransformableNode(arFragment.getTransformationSystem());

              this.placeObject(hit.createAnchor());
              break;
            }
          }
        }



        break;
      }
    } else {
      Vector3 prevPos = this.mTransformNode.getWorldPosition();
      Vector3 newPos = new Vector3(prevPos.x + offset, prevPos.y, prevPos.z);
      this.mTransformNode.setWorldPosition(newPos);

//      if (prevPos.x + offset >= 1 || prevPos.x + offset <= -0.5) {
//        this.offset = -this.offset;
//      }
    }

//    arFragment.getArSceneView().getScene().addOnUpdateListener(this.updateListener);

//    Session session = arFragment.getArSceneView().getSession();
//
//            /*float[] position = {0, 0, -1};
//            float[] rotation = {0, 0, 0, 1};
//            Anchor anchor = session.createAnchor(new Pose(position, rotation));*/
//
//    Vector3 cameraPos = arFragment.getArSceneView().getScene().getCamera().getWorldPosition();
//    Vector3 cameraForward = arFragment.getArSceneView().getScene().getCamera().getForward();
//    Vector3 position = Vector3.add(cameraPos, cameraForward.scaled(1.0f));
//
//    // Create an ARCore Anchor at the position.
//    Pose pose = Pose.makeTranslation(position.x, position.y, position.z);
//    Anchor anchor = session.createAnchor(pose);
//
//    mAnchorNode = new AnchorNode(anchor);
//    mAnchorNode.setParent(arFragment.getArSceneView().getScene());
//
//     /* Node node = new Node();
//      node.setRenderable(andyRenderable);
//      node.setParent(mAnchorNode);
//      node.setOnTapListener((hitTestResult, motionEvent) -> {
//      });*/
//
//    TransformableNode transformableNode = new TransformableNode(arFragment.getTransformationSystem());
//    transformableNode.setRenderable(andyRenderable);
//    transformableNode.setParent(mAnchorNode);
  };

  private void placeObject(Anchor anchor) {
    ModelRenderable.builder()
            .setSource(arFragment.getContext(), R.raw.andy)
            .build()
            .thenAccept(modelRenderable -> addNodeToScene(anchor, modelRenderable))
            .exceptionally(throwable -> {
              Toast toast =
                      Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG);
              toast.setGravity(Gravity.CENTER, 0, 0);
              toast.show();
              return null;
            });
  }

  private void addNodeToScene(Anchor createAnchor, ModelRenderable renderable) {
    AnchorNode anchorNode = new AnchorNode(createAnchor);
    TransformableNode transformableNode = new TransformableNode(arFragment.getTransformationSystem());
    anchorNode.setParent(arFragment.getArSceneView().getScene());
    transformableNode.setName("Andy");
    transformableNode.setRenderable(andyRenderable);
    transformableNode.setParent(anchorNode);

    arFragment.getArSceneView().getScene().addChild(anchorNode);

//    transformableNode.setOnTapListener((hitTestResult, motionEvent) -> {
//      //Perform callback action, like bark
//    });

    if (!this.isDone) {
      transformableNode.setWorldPosition(new Vector3(anchorNode.getWorldPosition().x + 0.9f, anchorNode.getWorldPosition().y + 0.2f, anchorNode.getWorldPosition().z + 0f));
//      this.isDone = true;
    } else {
      Vector3 prevPos = this.mTransformNode.getWorldPosition();
      Vector3 newPos = new Vector3(prevPos.x + this.offset, prevPos.y, prevPos.z);
      transformableNode.setWorldPosition(newPos);
    }
//    transformableNode.setLocalPosition(new Vector3(0.5f, 0.4f, 0.5f));
//    arFragment.getArSceneView().getScene().addOnUpdateListener(this.updateListener);
    this.mAnchorNode = anchorNode;
    this.mTransformNode = transformableNode;
//    this.isPlaced = true;
  }

  private Point getScreenCenter() {

    if(arFragment == null || arFragment.getView() == null) {
      return new android.graphics.Point(0,0);
    }

    int w = arFragment.getView().getWidth()/2;
    int h = arFragment.getView().getHeight()/2;
    return new android.graphics.Point(w, h);
  }

  /**
   * Returns false and displays an error message if Sceneform can not run, true if Sceneform can run
   * on this device.
   *
   * <p>Sceneform requires Android N on the device as well as OpenGL 3.0 capabilities.
   *
   * <p>Finishes the activity if Sceneform can not run
   */
  public static boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
    if (Build.VERSION.SDK_INT < VERSION_CODES.N) {
      Log.e(TAG, "Sceneform requires Android N or later");
      Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG).show();
      activity.finish();
      return false;
    }
    String openGlVersionString =
        ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE))
            .getDeviceConfigurationInfo()
            .getGlEsVersion();
    if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
      Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later");
      Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
          .show();
      activity.finish();
      return false;
    }
    return true;
  }
}
