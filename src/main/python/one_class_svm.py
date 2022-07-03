from sklearn.svm import OneClassSVM
from sklearn.datasets import make_blobs
from numpy import where, random
import matplotlib.pyplot as plt

random.seed(13)
x, _ = make_blobs(n_samples=200, centers=1, cluster_std=.3, center_box=(8, 8))

plt.scatter(x[:,0], x[:,1])
plt.show()

svm = OneClassSVM(
  kernel='rbf', # 'linear', 'poly', 'rbf', 'sigmoid', 'precomputed'
  gamma=0.001, 
  nu=0.03
)

pred = svm.fit_predict(x)

print(pred)

anom_index = where(pred==-1)
values = x[anom_index]

plt.scatter(x[:,0], x[:,1])
plt.scatter(values[:,0], values[:,1], color='r')
plt.show()
