package com.arpadfodor.stolenvehicledetector.android.app.model.ml.detector

import android.content.res.AssetManager

class EfficientDetD1(assets: AssetManager, threads: Int) : ObjectDetector(
    assets,
    threads,

    /**
     * Not overriding variables due to super constructor inconsistency: singleton's class is loaded with its static backing fields already initialized with consts
     * More info: https://youtrack.jetbrains.com/issue/KT-21764
     */

    // Model and label paths
    BASE_PATH = "detector/EfficientDetD1/",
    MODEL_PATH = "model.tflite",
    LABEL_PATH = "labelmap.txt",

    // Whether the model can run on GPU
    GPU_INFERENCE_SUPPORT = false,

    // image properties
    IMAGE_MEAN = 127.5f,
    IMAGE_STD = 127.5f,

    // Input image size required by the model
    IMAGE_SIZE_X = 640,
    IMAGE_SIZE_Y = 640,
    // Input image channels (RGB)
    DIM_CHANNEL_SIZE = 3,
    // Number of bytes of a channel in a pixel
    // 1 means the model is quantized (Int), 4 means non-quantized (floating point)
    NUM_BYTES_PER_CHANNEL = 4,

    // batch size dimension
    DIM_BATCH_SIZE = 1,
    // returns this many results
    NUM_DETECTIONS = 100
    ) {

}