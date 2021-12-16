package com.arpadfodor.stolenvehicledetector.android.app.model.ml.detector

import android.content.res.AssetManager
import com.arpadfodor.stolenvehicledetector.android.app.model.ml.Preprocessing

class ResBlurNet(assets: AssetManager, threads: Int) : ObjectDetector(
    assets,
    threads,

    // Model and label paths
    BASE_PATH = "detector/ResBlurNet/",
    MODEL_PATH = "model.tflite",
    LABEL_PATH = "labelmap.txt",

    GPU_INFERENCE_SUPPORT = false,

    PREPROCESSING = Preprocessing.STANDARDIZE,
    IMAGE_MEAN = 127.5f,
    IMAGE_STD = 127.5f,

    IMAGE_SIZE_X = 256,
    IMAGE_SIZE_Y = 256,
    NUM_CHANNELS = 3,
    NUM_BYTES_PER_CHANNEL = 4,
    BATCH_SIZE = 1,

    // returns this many results
    NUM_DETECTIONS = 100
)