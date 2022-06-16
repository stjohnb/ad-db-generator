from sklearn import datasets
from sklearn.model_selection import train_test_split # Import train_test_split function
from sklearn import svm
from sklearn import metrics # Import scikit-learn metrics module for accuracy calculation

cancer = datasets.load_breast_cancer() #Load dataset
print("Features: ", cancer.feature_names) # print the names of the 13 features
print("Labels: ", cancer.target_names) # print the label type of cancer('malignant' 'benign')
print("data(feature)shape: ", cancer.data.shape) # print data(feature)shape
print(cancer.data[0:5]) # print the cancer data features (top 5 records)
print(cancer.target) # print the cancer labels (0:malignant, 1:benign)


# Split dataset into training set and test set
X_train, X_test, y_train, y_test = train_test_split(cancer.data, cancer.target, test_size=0.3,random_state=109) # 70% training and 30% test

clf = svm.SVC(kernel='linear') # Create a svm Classifier - Linear Kernel
clf.fit(X_train, y_train) # Train the model using the training sets
y_pred = clf.predict(X_test) # Predict the response for test dataset

print("Accuracy:",metrics.accuracy_score(y_test, y_pred)) # Model Accuracy: how often is the classifier correct?
print("Precision:",metrics.precision_score(y_test, y_pred)) # Model Precision: what percentage of positive tuples are labeled as such?
print("Recall:",metrics.recall_score(y_test, y_pred)) # Model Recall: what percentage of positive tuples are labelled as such?
