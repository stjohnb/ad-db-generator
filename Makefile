gen:
	sbt run

init:
	pip install -r requirements.txt

run:
	python src/main/python/one_class_svm.py

full:
	make gen && make init && make run

.PHONY: gen init run
