.PHONY: all check clean

all:
	cd java && $(MAKE) all
	cd backend && $(MAKE) all
	cp java/dist/lljvm.jar backend/lljvm-backend .

check: all
	cd test && $(MAKE) -s check

clean:
	cd java && $(MAKE) clean
	cd backend && $(MAKE) clean
	cd test && $(MAKE) clean
	rm -f lljvm.jar lljvm-backend
