init:
	pip install -r requirements.txt

run:
	python src/main/python/lateral_movement_svm.py

.PHONY: init test
