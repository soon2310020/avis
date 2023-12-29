from tempbasedWarmup import expFitter
from wutDashboardService import calculateWarmUpTime
from flask import Flask, request, jsonify
from flask_restful import Resource, Api

app = Flask(__name__)
api = Api(app)

class Calculator(Resource):
	def post(self):
		results = []
		data = [jsonify(i) for i in request.json['data']]
		for i in range(len(data)):
			index = int(data[i].json['index'])
			temp = [float(t) for t in data[i].json['temp']]
			result = expFitter(temp, 0.03, 0.005)
			result['index'] = index
			results.append(result)
		myResult = {'data': results}
#		result = {'data': [expFitter(temp, 0.03, 0.005)]}
#		result = expFitter(temp, 0.03, 0.005)
#		print (result)
		return jsonify(myResult)
		
class Test(Resource):
	def post(self):
		return jsonify(request.json);

class WarmupTime(Resource):
    def post(self):
        data = request.json['data']
        print(data)
        result = {'data': calculateWarmUpTime(data)}
        return result


api.add_resource(Test, '/test')
api.add_resource(Calculator, '/calculator')
api.add_resource(WarmupTime, '/warmup-time')


if __name__ == '__main__':
	app.run(host='0.0.0.0')