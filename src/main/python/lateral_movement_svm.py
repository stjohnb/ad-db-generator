from sklearn.model_selection import train_test_split 
from sklearn import svm
from sklearn import metrics
import pandas

lateral_movement_csv = pandas.read_csv('feature-vectors/all-changes.csv')

classes = lateral_movement_csv['isLateralMovement']
features = lateral_movement_csv.drop(['userId', 'isLateralMovement'], axis=1)

x_train, x_test, y_train, y_test = train_test_split(features, classes, test_size=0.3,random_state=109) # 70% training and 30% test

clf = svm.SVC(kernel='linear') # 'linear', 'poly', 'rbf', 'sigmoid', 'precomputed'
clf.fit(x_train, y_train)
y_pred = clf.predict(x_test)

print("Accuracy:", metrics.accuracy_score(y_test, y_pred))
print("Precision:", metrics.precision_score(y_test, y_pred))
print("Recall:", metrics.recall_score(y_test, y_pred))
