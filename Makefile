init:
	pip install -r requirements.txt

run:
	python src/main/python/one_class_svm.py

.PHONY: init test
