package com.arpadfodor.stolenvehicledetector.android.app.model.ml.ocr

import android.content.res.AssetManager
import com.arpadfodor.stolenvehicledetector.android.app.model.ml.Normalization

class ResidualCRNN(assets: AssetManager, threads: Int) : OCR(
    assets,
    threads,

    // Model and label paths
    BASE_PATH = "OCR/ResidualCRNN/",
    MODEL_PATH = "model.tflite",
    LABEL_PATH = "characters.txt",

    GPU_INFERENCE_SUPPORT = true,

    NORMALIZATION = Normalization.NORMALIZE,
    IMAGE_MEAN = 127.5f,
    IMAGE_STD = 127.5f,

    IMAGE_SIZE_X = 500,
    IMAGE_SIZE_Y = 50,
    NUM_CHANNELS = 3,
    NUM_BYTES_PER_CHANNEL = 4,
    BATCH_SIZE = 1,

    // output properties
    // returns this many text blocks
    NUM_BLOCKS = 1,
    // maximum text block length
    MAX_BLOCK_LENGTH = 125,
    // returns this many char probabilities
    NUM_CHARACTERS = 38
)