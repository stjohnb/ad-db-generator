from cgi import test
from cmath import e
from statistics import mean
from scipy import rand
from sklearn.svm import OneClassSVM
import pandas
from sklearn import metrics
import matplotlib.pyplot as plt
import numpy as np
from pathlib import Path
import shutil

run_count = 10

def process_scenario(scaling_factor, kernel):
  runs = range(1, run_count + 1)

  results = np.array(list(map(lambda run: process_run(scaling_factor, run, kernel), runs)))

  return [
    sum(results[:, 0]) / run_count,
    sum(results[:, 1]) / run_count,
    sum(results[:, 2]) / run_count,
    sum(results[:, 3]) / run_count,
  ]


def process_run(scaling_factor, run, kernel):
  path = f'feature-vectors/randomness-{scaling_factor}_run-{run}'

  train = pandas.read_csv(f'{path}/train.csv')
  test = pandas.read_csv(f'{path}/test.csv')

  print(f'scaling_factor={scaling_factor} train_samples_count={len(train)}')

  train_features = train.drop(['userId', 'isNormalActivity'], axis=1)
  test_labels = test['isNormalActivity']

  svm = OneClassSVM(
    kernel=kernel, # 'linear', 'poly', 'rbf', 'sigmoid', 'precomputed'
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

kernels = ['linear', 'poly', 'rbf', 'sigmoid']
scaling_factors = range(1, 21)

shutil.rmtree('target/plots')

for kernel in kernels:
  results = np.array(list(map(lambda scaling_factor: process_scenario(scaling_factor, kernel), scaling_factors)))

  plots_dir = f'target/plots/{kernel}'

  Path(plots_dir).mkdir(parents=True, exist_ok=True)

  plt.title(f'Anomaly detection - SVM(kernel={kernel})')
  plt.xlabel("Scaling factor")
  plt.ylabel(f'Mean accuracy score over {run_count} runs')
  plt.ylim([0, 1])
  plt.plot(scaling_factors, results[:, 0], color='red')
  plt.savefig(f'{plots_dir}/Accuracy.png')
  plt.clf()

  plt.title(f'Anomaly detection - SVM(kernel={kernel})')
  plt.xlabel("Scaling factor")
  plt.ylabel(f'Mean precision score over {run_count} runs')
  plt.ylim([0, 1])
  plt.plot(scaling_factors, results[:, 1], color='green')
  plt.savefig(f'{plots_dir}/Precision.png')
  plt.clf()

  plt.title(f'Anomaly detection - SVM(kernel={kernel})')
  plt.xlabel("Scaling factor")
  plt.ylabel(f'Mean recall score over {run_count} runs')
  plt.ylim([0, 1])
  plt.plot(scaling_factors, results[:, 2], color='green')
  plt.savefig(f'{plots_dir}/Recall.png')
  plt.clf()

  plt.title(f'Anomaly detection - SVM(kernel={kernel})')
  plt.xlabel("Scaling factor")
  plt.ylabel(f'Mean F1 score over {run_count} runs')
  plt.ylim([0, 1])
  plt.plot(scaling_factors, results[:, 3], color='green')
  plt.savefig(f'{plots_dir}/F1.png')
  plt.clf()

  print(f'kernel={kernel} - Mean accuracy: {mean(results[:, 1])}')
  print(f'kernel={kernel} - Mean precision: {mean(results[:, 2])}')
  print(f'kernel={kernel} - Mean recall: {mean(results[:, 3])}')
  print(f'kernel={kernel} - Mean F1: {mean(results[:, 3])}')
  
