# Detector

This is the repository of the detector algorithm. The model is based on a Convolutional Neural Network used in an SSD architecture, and built with TensorFlow 2.3 Object Detection API. 

- Find the data preparation process in */data* subfolder. Note that the dataset itself is not uploaded due to its size, but image samples are uploaded. A Power BI report is also accessible that visualizes the data.
- The model building and training procedure can be found in the *Model training* Notebook. The *Model inference* file is used to run inference on a selected model. The *TFLite converter* Notebook does the Tensorflow -> TFLite conversion.

