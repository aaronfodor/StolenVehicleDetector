package com.arpadfodor.stolenvehicledetector.android.app.model.ml.detector

import android.content.res.AssetManager
import com.arpadfodor.stolenvehicledetector.android.app.model.ml.Preprocessing

class MobileNetV3Coco(assets: AssetManager, threads: Int) : ObjectDetector(
    assets,
    threads,

    // Model and label paths
    BASE_PATH = "detector/MobileNetV3Coco/",
    MODEL_PATH = "model.tflite",
    LABEL_PATH = "labelmap.txt",

    GPU_INFERENCE_SUPPORT = true,

    PREPROCESSING = Preprocessing.NORMALIZE,
    IMAGE_MEAN = 128f,
    IMAGE_STD = 128f,

    IMAGE_SIZE_X = 320,
    IMAGE_SIZE_Y = 320,
    NUM_CHANNELS = 3,
    NUM_BYTES_PER_CHANNEL = 4,
    BATCH_SIZE = 1,

    // returns this many results
    NUM_DETECTIONS = 100
)