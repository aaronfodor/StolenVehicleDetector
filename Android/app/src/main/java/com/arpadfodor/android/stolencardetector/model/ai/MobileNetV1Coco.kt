package com.arpadfodor.android.stolencardetector.model.ai

import android.content.res.AssetManager

class MobileNetV1Coco(assets: AssetManager, threads: Int) : ObjectDetector(
    assets,
    threads,

    /**
     * Not overriding variables due to super constructor inconsistency: singleton's class is loaded with its static backing fields already initialized with consts
     * More info: https://youtrack.jetbrains.com/issue/KT-21764
     */

    // Model and label paths
    MODEL_PATH = "model.tflite",
    LABEL_PATH = "labelmap.txt",

    // float model
    IMAGE_MEAN = 127.5f,
    IMAGE_STD = 127.5f,

    // Input image size required by the model
    IMAGE_SIZE_X = 300,
    IMAGE_SIZE_Y = 300,
    // Input image channels (RGB)
    DIM_CHANNEL_SIZE = 3,
    // Number of bytes of a channel in a pixel
    // 1 means the model is quantized (Int), 4 means non-quantized (floating point)
    NUM_BYTES_PER_CHANNEL = 1,

    // batch size dimension
    DIM_BATCH_SIZE = 1,
    // returns this many results
    NUM_DETECTIONS = 10
    ) {

}