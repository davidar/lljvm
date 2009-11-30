all:
	cd java && $(MAKE) all
	cd backend && $(MAKE) all
	cp java/dist/lljvm.jar backend/lljvm-backend .

clean:
	cd java && $(MAKE) clean
	cd backend && $(MAKE) clean
	rm -f lljvm.jar lljvm-backend
