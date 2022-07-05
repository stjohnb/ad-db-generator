from cgi import test
from cmath import e
from scipy import rand
from sklearn.svm import OneClassSVM
import pandas
from sklearn import metrics
import matplotlib.pyplot as plt
import numpy as np

run_count = 10

def process_scenario(randomness):
  runs = range(1, run_count + 1)

  results = np.array(list(map(lambda run: process_run(randomness, run), runs)))

  return [
    sum(results[:, 0]) / run_count,
    sum(results[:, 1]) / run_count,
    sum(results[:, 2]) / run_count,
    sum(results[:, 3]) / run_count,
  ]

def process_run(randomness, run):
  path = f'feature-vectors/randomness-{randomness}_run-{run}'

  train = pandas.read_csv(f'{path}/train.csv')
  test = pandas.read_csv(f'{path}/test.csv')

  train_features = train.drop(['userId', 'isNormalActivity'], axis=1)
  train_labels = train['isNormalActivity']
  test_labels = test['isNormalActivity']

  svm = OneClassSVM(
    kernel='rbf', # 'linear', 'poly', 'rbf', 'sigmoid', 'precomputed'
    gamma=0.001, 
    nu=0.03
  )

  svm.fit(train_features)

  test_pred = svm.predict(test.drop(['userId', 'isNormalActivity'], axis=1))

  return [
    metrics.accuracy_score(test_labels, test_pred),
    metrics.precision_score(test_labels, test_pred),
    metrics.recall_score(test_labels, test_pred),
    metrics.f1_score(test_labels, test_pred)
  ]

randomness = range(1, 21)

results = np.array(list(map(process_scenario, randomness)))

plt.title("Anomaly detection")
plt.xlabel("Randomness")
plt.ylabel(f'Mean accuracy score over {run_count} runs')
plt.ylim([0, 1])
plt.plot(randomness, results[:, 0], color='red')
plt.savefig('target/plots/Accuracy.png')
plt.clf()

plt.title("Anomaly detection")
plt.xlabel("Randomness")
plt.ylabel(f'Mean precision score over {run_count} runs')
plt.ylim([0, 1])
plt.plot(randomness, results[:, 1], color='green')
plt.savefig('target/plots/Precision.png')
plt.clf()

plt.title("Anomaly detection")
plt.xlabel("Randomness")
plt.ylabel(f'Mean recall score over {run_count} runs')
plt.ylim([0, 1])
plt.plot(randomness, results[:, 2], color='green')
plt.savefig('target/plots/Recall.png')
plt.clf()

plt.title("Anomaly detection")
plt.xlabel("Randomness")
plt.ylabel(f'Mean F1 score over {run_count} runs')
plt.ylim([0, 1])
plt.plot(randomness, results[:, 3], color='green')
plt.savefig('target/plots/F1.png')
plt.clf()
