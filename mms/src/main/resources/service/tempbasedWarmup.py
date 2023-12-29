import numpy as np
from scipy import optimize


# Data fitter to the form y = a*exp(b*x) + c
# obj: objective function
# temp: temprature data
# cutOffSlope: slope of fitted curve that determines the end of warmup time
# tolerance: margin of error when selecting the cutOffSlope
    
def expFitter(temp, cutOffSlope, tolerance):
    
    # Form of Fitted Curve
    def obj(time, a, b, c):
        return a*np.exp(b*time)+c
    
    time  = np.arange(0,np.size(temp)*10, 10)
    p0 = [min(temp) - max(temp), -0.01, max(temp)]                      # Intial Guess of Coefficients 
    parms, params_cov = optimize.curve_fit(obj, time, temp, p0, maxfev=5000)
    
    def diff_Fitted(time):                                          # Finds the derivative of the fitted curve at 10 minutes interval
        a = parms[0]
        b = parms[1]
        diffVal =  a*b*np.exp(b*time)
        margin = np.abs(diffVal - cutOffSlope)

        idx_margin_Temp = 0
        for m in range(len(margin)):
            if margin[m] < tolerance:
                idx_margin_Temp = m
                break
            
        durationofWarmUp = (idx_margin_Temp+1)*10                  # duration of warmup time in minutes
        
        return durationofWarmUp	
    
#    return parms, int(diff_Fitted(time)/10-1), diff_Fitted(time), obj(time, parms[0], parms[1], parms[2])
    result = {'coefficients': [float(i) for i in parms], 'indexWUT': int(diff_Fitted(time)/10-1), 'wut': diff_Fitted(time), 'temp': [float(i) for i in obj(time, parms[0], parms[1], parms[2])]}
    return result


# parms = [a, b, c] ---> list of coefficients 
# int(diff_Fitted(time)/10-1) = the index of the end of warmup time for a given uptime
# diff_Fitted(time) = duration of warm up time in minutes
#  obj(time, parms[0], parms[1], parms[2]): values of fitted curve for the corresponding real temp data 





 
    

