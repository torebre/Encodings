How to compile and get Java-bindings: https://docs.opencv.org/4.x/d9/d52/tutorial_java_dev_intro.html

Get the code
```
git clone git://github.com/opencv/opencv.git
cd opencv
mkdir build
cd build
```

Need to specify output directory for binaries and source directory to avoid getting an error message about in-source build not allowed
```
cmake -DBUILD_SHARED_LIBS=OFF -B /home/student/workspace/testEncodings/opencv_build/opencv/build -S /home/student/workspace/testEncodings/opencv_build/opencv/
```

Compile the code
```
make -j8
```