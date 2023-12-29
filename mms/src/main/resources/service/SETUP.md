## Setup service wut

Envirment: python2.7<br>

Install lib for tempbasedWarmup:<br>
- pip install numpy<br> (python3 -m pip install numpy)
- pip install scipy<br>
- pip install flask<br>
- pip install flask_restful<br>
  <br>
  Install lib for productionPatternAnalysis:<br>
- pip install pandas<br>
- pip install matplotlib<br>
- pip install flask<br>
- pip install flask_restful<br>
  <br>

  Code for deploy: **src/resource/service**
  <br>
  Run on server: **python main.py**

or
**nohup python3 main.py 1> nohup.out 2>&1 &**

shuttdown:
**sudo fuser -n tcp -k 5000 || true**
