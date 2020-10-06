# Data report

This is the folder of the Power BI report that visualizes the dataset used to train/evaluate the detector network. The report can be used to analyze the raw and the splitted dataset as well. Change the data source folder to *source/raw* or to *source/splitted* accordingly.



Distribution of images per label types:

<img src="assets/report1.PNG" style="zoom:60%;" />



Image dimensions in the test set containing Vehicle bounding boxes:

<img src="assets/report2.PNG" style="zoom:60%;" />



Average & median Vehicle registration plate bounding box coordinates:

<img src="assets/report3.PNG" style="zoom:60%;" />



The report was used to split the data evenly, as the original Open Images Dataset train/validation/test subsets had different bounding box coordinate distributions & multiplicities per images.