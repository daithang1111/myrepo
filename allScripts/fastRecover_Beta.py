import time
import sys
from numpy import *
import multiprocessing
import math
import scipy
#from gurobipy import *

def do_recovery(Q, anchors, loss, params):
    if loss == "originalRecover":
        return (Recover(Q, anchors), None)
    elif loss == "KL" or "L2" in loss:
	V = Q.shape[0]
	K = len(anchors)
	C = dirichletDraw(V,K) #matrix(ones((V,K)))/K
	T =ones(V)
	#originQ =copy(Q)
	#print "Q:", Q
 	#Q =Q+ 10**(-9)
	originQ =copy(Q)
    	for v in xrange(V):		
        	T[v] = Q[v,:].sum() #Thang
        print "T min : ",T.min()
	print "Vocab size : ", V 
	rLambda =0.01
	tmpLambda =rLambda
	numOfLoop=15
	if tmpLambda == 0:
		numOfLoop =1
        #for bloop in xrange(numOfLoop):
	bloop =1
	while 1:
		print "boostrap bloop =",bloop
		C, A, colsums = nonNegativeRecover(rLambda,T, C, Q, anchors, params.log_prefix, loss, params.max_threads, epsilon=params.eps)
        	#diff =diffC(C,C1)
		#print "DiffC: ", diff
		#if diff <1:
		#	break
		if bloop >numOfLoop:
			break
		bloop+=1
		#C=C1
		hp = colsums
		Q =copy(originQ)
		#rLambda +=0.01
		#C=dirichletDraw(V,K)
		#print "MATRIX C: ", C
        return A, hp
    else:
        print "unrecognized loss function", loss, ". Options are KL,L2 or originalRecover"
        
        return None

def diffC(C1, C2):

	D = C1 -C2
	sum =0.0
	for v in xrange(D.shape[1]):
		tmp = linalg.norm(D[:,v])
		sum+= tmp*tmp	
	return math.sqrt(sum)
def dirichletDraw(V, K):
	x = ones(V)/V
	x = x*60
	y= random.dirichlet(x, K).transpose()
	#normalize y
	for i in xrange(K):
		y[i,:]/= y[i,:].sum()
	return y
	
def logsum_exp(y):
    m = y.max()
    return m + log((exp(y - m)).sum())

def KL_helper(arg):
    p,q = arg
    if p == 0:
        return 0
    return p*(log(p)-log(q))

def entropy(p):
    e = 0
    for i in xrange(len(p)):
        if p[i] > 0:
            e += p[i]*log(p[i])
    return -e
        

def KL(p,log_p,q):
    N = p.size
    ret = 0
    log_diff = log_p - log(q)
    ret = dot(p, log_diff)
    if ret < 0 or isnan(ret):
        print "invalid KL!"
        print "p:"
        for i in xrange(n):
            print p[i]
            if p[i] <= 0:
                print "!!"
        print "\nq:"
        for i in xrange(n):
            print q[i]
            if q[i] <= 0:
                print "!!"
        if ret < 0:
            print "less than 0", ret
        sys.exit(1)
    return ret


#this method does not use a line search and as such may be faster
#but it needs an initialization of the stepsize
def fastQuadSolveExpGrad(y, x, eps, initialStepsize, recoveryLog, anchorsTimesAnchors=None): 
    (K,n) = x.shape

    # Multiply the target vector y and the anchors matrix X by X'
    #  (XX' could be passed in as a parameter)
    if anchorsTimesAnchors==None:
        print "XX' was not passed in"
        anchorsTimesAnchors = dot(x, x.transpose())
    targetTimesAnchors = dot(y, x.transpose())

    alpha = ones(K)/K
    log_alpha = log(alpha)

    iteration = 1 
    eta = 0.1 

    # To get the gradient, do one K-dimensional matrix-vector product
    proj = -2*(targetTimesAnchors - dot(alpha,anchorsTimesAnchors))
    new_obj = linalg.norm(proj,2)
    gap = float('inf')

    while 1:
    # Set the learning rate
        eta = initialStepsize/(iteration**0.5)
        iteration += 1

        # Save previous values for convergence tests
        old_obj = new_obj
        old_alpha = copy(alpha)

        # Add the gradient and renormalize in logspace, then exponentiate
        log_alpha += -eta*proj
        log_alpha -= logsum_exp(log_alpha)

        alpha = exp(log_alpha)
        
        # Recalculate the gradient and check for convergence
        proj = -2*(targetTimesAnchors - dot(alpha,anchorsTimesAnchors))
        new_obj = linalg.norm(proj,2)

        # Stop if the L2 norm of the change in alpha OR 
        #  the % change in L2 norm of the gradient are below tolerance.
        #convergence = min(linalg.norm(alpha-old_alpha, 2), abs(new_obj-old_obj)/old_obj)
        
        # stop if the primal-dual gap < eps
        lam = copy(proj)
        lam -= lam.min()

        gap = dot(alpha, lam)

        if gap < eps and iteration > 1:
            break

        if iteration % 10000 == 0:
            print  "iter", iteration, "obj", old_obj, "gap", gap

    return alpha, iteration, new_obj, None, gap

def quadSolveExpGrad(y, x, eps, alpha=None, XX=None): 
    c1 = 10**(-4)
    c2 = 0.75
    if XX == None:
        print 'making XXT'
        XX = dot(x, x.transpose())

    XY = dot(x, y)
    YY = float(dot(y, y))

    start_time = time.time()
    y_copy = copy(y)
    x_copy = copy(x)

    (K,n) = x.shape
    if alpha == None:
        alpha = ones(K)/K

    old_alpha = copy(alpha)
    log_alpha = log(alpha)
    old_log_alpha = copy(log_alpha)

    it = 1 
    aXX = dot(alpha, XX)
    aXY = float(dot(alpha, XY))
    aXXa = float(dot(aXX, alpha.transpose()))

    grad = 2*(aXX-XY)
    new_obj = aXXa - 2*aXY + YY

    old_grad = copy(grad)

    stepsize = 1
    repeat = False
    decreased = False
    gap = float('inf')
    while 1:
        eta = stepsize
        old_obj = new_obj
        old_alpha = copy(alpha)
        old_log_alpha = copy(log_alpha)
        if new_obj == 0:
            break
        if stepsize == 0:
            break

        it += 1
        #if it % 1000 == 0:
        #    print "\titer", it, new_obj, gap, stepsize
        #update
        log_alpha -= eta*grad
        #normalize
        log_alpha -= logsum_exp(log_alpha)
        #compute new objective
        alpha = exp(log_alpha)

        aXX = dot(alpha, XX)
        aXY = float(dot(alpha, XY))
        aXXa = float(dot(aXX, alpha.transpose()))

        old_obj = new_obj
        new_obj = aXXa - 2*aXY + YY
        if not new_obj <= old_obj + c1*stepsize*dot(grad, alpha - old_alpha): #sufficient decrease
            stepsize /= 2.0 #reduce stepsize
            alpha = old_alpha 
            log_alpha = old_log_alpha
            new_obj = old_obj
            repeat = True
            decreased = True
            continue

        #compute the new gradient
        old_grad = copy(grad)
        grad = 2*(aXX-XY)
        
        if (not dot(grad, alpha - old_alpha) >= c2*dot(old_grad, alpha-old_alpha)) and (not decreased): #curvature
            stepsize *= 2.0 #increase stepsize
            alpha = old_alpha
            log_alpha = old_log_alpha
            grad = old_grad
            new_obj = old_obj
            repeat = True
            continue

        decreased = False

        lam = copy(grad)
        lam -= lam.min()
        
        gap = dot(alpha, lam)
        convergence = gap
        if (convergence < eps):
            break

    return alpha, it, new_obj, stepsize, gap



def KLSolveExpGrad(rLambda,T, C, y,x,v,eps, alpha=None):
    #if v >1:
    #	sys.exit(0)
    #print "T: ", T
    #print "C: ", C
    #if v%1000==0:
	#print "C:",C
    s_t = time.time()
    c1 = 10**(-4)
    c2 = 0.9
    it = 1 
    
    start_time = time.time()
    y = clip(y, 0, 1)
    x = clip(x, 0, 1)

    (K,N) = x.shape
    mask = list(nonzero(y)[0])

    y = y[mask]
    x = x[:, mask]

    x += 10**(-9)
    x /= x.sum(axis=1)[:,newaxis]
    
    V = C.shape[0]
    #print "V: ",V
    varALPHA = 60.0
    alphaBeta = 1.0 +(varALPHA/V)
    betaBeta =varALPHA #*((V-1)/V)+1
    #print "alphaBeta:",alphaBeta
    #print "TC sample = ", TC[0:10]
    if alpha == None:
        alpha = ones(K)/K
	#alpha = random.dirichlet(alpha, 1).transpose()
 	
    old_alpha = copy(alpha)
    log_alpha = log(alpha)
    old_log_alpha = copy(log_alpha)
    proj = dot(alpha,x)
    old_proj = copy(proj)

    log_y = log(y)
    #new_obj = KL(y,log_y, proj)
    new_C = copy(C)
    #print "new_C: ",new_C
    #calculate CONST for gamma
    #gammaAlpha1 = math.gamma(alphaBeta)
    #gammaAlpha2 = math.gamma((V-1)*alphaBeta)
    #gammaAlpha3 = math.gamma(V*alphaBeta)
    #aConst = log(gammaAlpha1)+log(gammaAlpha2) -log(gammaAlpha3)
    #aConst *=K
    aConst =0.0
    #print "aConst:", aConst
    #print new_C.shape
    new_C[v,:] =copy(alpha)
    #print "T.shape: ", T.shape
    #print "new_C.shape: ",new_C.shape
    #print "T min = ",T.min()
    TC = dot(T,new_C)
    #print "TC: ", TC
    J2 = -aConst
    for k in xrange(K):
    	cikti_tck = (new_C[v,k]*T[v])/TC[k]
    	if cikti_tck >0: 
    		J2+=(alphaBeta-1)*log(cikti_tck) +(betaBeta-1)*log(1-cikti_tck)

    new_obj = KL(y, log_y, proj) -rLambda*J2
    #print "new_obj: ", new_obj
    y_over_proj = y/proj
    #grad = -dot(x, y_over_proj.transpose())
    #direvative of J2
    #if v%1000==0:
    #print "ALPHA: ",alpha
    alphaBeta_over_C =(alphaBeta-1)/alpha
    #print "alphaBeta_over_C: ",alphaBeta_over_C
    #print "T[v]: ", T[v]
    alphaBetaV_over_TC = ((2- alphaBeta-betaBeta)*T[v])/TC
    #print "alphaBetaV_over_TC: ", alphaBetaV_over_TC
    grad = -dot(x,y_over_proj) - rLambda*(alphaBeta_over_C + alphaBetaV_over_TC) 
    #if v%1000==0:
    #print "GRAD:",grad
    old_grad = copy(grad)
    stepsize =1   #THANG
    decreasing = False
    repeat = False
    gap = float('inf')
    tmpCounter =1
    while 1:#tmpCounter <1000: #1:
        tmpCounter+=1
        eta = stepsize
        old_obj = new_obj
        old_alpha = copy(alpha)
        old_log_alpha = copy(log_alpha)

        old_proj = copy(proj)

        it += 1
        #take a step
        log_alpha -= eta*grad
	#log_alpha +=log(1+exp(-eta*grad))
        #normalize
        log_alpha -= logsum_exp(log_alpha)

        #compute new objective
        alpha = exp(log_alpha)
	if alpha.min()<10**(-100):
		if v%1000 ==0:
			print "MIN entry of ALPHA (C_i) is too small, MAX = ", alpha.max()
		break
	
	#if v==0 or v==1:
	#	print "ETA::::",eta
	#	print "GRAD::::",grad
	#print "ALPHA::::",alpha
        proj = dot(alpha,x)
        
	#now let try to comment out this, consider no update on alpha 
	J2 =-aConst
    	new_C[v,:] =copy(alpha)
    	TC = dot(T,new_C)
    	for k in xrange(K):
    		cikti_tck = (new_C[v,k]*T[v])/TC[k]
        	if cikti_tck >0:
                	J2+=(alphaBeta-1)*log(cikti_tck) +(betaBeta-1)*log(1-cikti_tck)
	#print "J2-J2 = ",J2	
	new_obj = KL(y, log_y, proj) -rLambda*J2
	tmplambda =rLambda
        if new_obj < eps and tmplambda ==0:
                break

	#print "new_obj: ", new_obj
	#new_obj = KL(y,log_y,proj)
        #new_obj = KL(y,log_y,proj)+
        #if new_obj < eps:
	#    if v%100 ==0:
	#    	print "new_obj, eps ", new_obj, eps
        #    break

        grad_dot_deltaAlpha = dot(grad, alpha - old_alpha)
	#if v%1000 ==0:
	#    print "GRAD_DOT_DELTA:",grad_dot_deltaAlpha
        assert (grad_dot_deltaAlpha <= 10**(-9))
        if not new_obj <= old_obj + c1*stepsize*grad_dot_deltaAlpha: #sufficient decrease
            stepsize /= 2.0 #reduce stepsize
            #print "new_obj, old_obj:", new_obj, old_obj
	    if stepsize < 10**(-6):
		if v%1000==0:
			print"STEPSIZE threshold achieved, stepsize=",stepsize
                break
            alpha = old_alpha 
            log_alpha = old_log_alpha
            proj = old_proj
            new_obj = old_obj
            repeat = True
            decreasing = True
            continue

        
        #compute the new gradient
        old_grad = copy(grad)
        y_over_proj = y/proj
    	#commenting out this, no need to recompute
	new_C[v,:] =copy(alpha)
    	TC = dot(T,new_C)
    	
	#grad = -dot(x, y_over_proj.transpose())
    	#direvative of J2
	#if v%1000==0:
        #	print "ALPHA-1:",alpha
    	
	alphaBeta_over_C =(alphaBeta-1)/alpha
    	alphaBetaV_over_TC = ((2- alphaBeta-betaBeta)*T[v])/TC

    	grad = -dot(x,y_over_proj) - rLambda*(alphaBeta_over_C + alphaBetaV_over_TC)
    	#if v%1000==0:
        #print "GRAD-1: ",grad
	#grad = normalized_alpha-dot(x, y_over_proj)
        #grad = -dot(x, y_over_proj)

        if not dot(grad, alpha - old_alpha) >= c2*grad_dot_deltaAlpha and not decreasing: #curvature
            stepsize *= 2.0 #increase stepsize
            alpha = old_alpha
            log_alpha = old_log_alpha
            grad = old_grad
            proj = old_proj
            new_obj = old_obj
            repeat = True
            continue

        decreasing= False
        lam = copy(grad)
        lam -= lam.min()
        
        gap = dot(alpha, lam)
        convergence = gap
        if (convergence < eps):
	    if v%1000==0:
		print "CONVERGENGE of solver achieved, convergence:",convergence
		print "NEW OBJ:", new_obj
		print "OLD OBJ:", old_obj
		print "FINAL C:", alpha	
            break
    #print "TMP COUNTER:",tmpCounter
    return alpha, it, new_obj, stepsize, time.time()- start_time, gap


def Recover(Q, anchors):
    K = len(anchors)
    orig = Q
    #print "anchors", anchors
    #print "RECOVERY:"
    permutation = range(len(Q[:,0]))
    for a in anchors:
        permutation.remove(a)
    permutation = anchors + permutation
    Q_prime = Q[permutation, :]
    Q_prime = Q_prime[:, permutation]
    DRD = Q_prime[0:K, 0:K]
    DRAT = Q_prime[0:K, :]
    DR1 = dot(DRAT, ones(DRAT[0,:].size))
    z = linalg.solve(DRD, DR1)
    A = dot(linalg.inv(dot(DRD, diag(z))), DRAT).transpose()
    reverse_permutation = [0]*(len(permutation))
    for p in permutation:
        reverse_permutation[p] = permutation.index(p)
    A = A[reverse_permutation, :]
    return A

def fastRecover(args):
    rLambda, T,C,y,x,v,logfilename,anchors,divergence,XXT,initial_stepsize,epsilon = args
    start_time = time.time() 

    K = len(anchors)
    alpha = zeros(K)
    gap = None
    if v in anchors:
        alpha[anchors.index(v)] = 1
        it = -1
        dist = 0
        stepsize = 0
	#print "v = ",v
    else:
        try:
            if divergence == "KL":
                alpha, it, dist, stepsize, t, gap = KLSolveExpGrad(rLambda,T, C, y, x,v, epsilon)
            elif divergence == "L2":
                alpha, it, dist, stepsize, gap = quadSolveExpGrad(y, x, epsilon, None, XXT)
            elif divergence == "fastL2":
                alpha, it, dist, stepsize, gap = fastQuadSolveExpGrad(y, x, epsilon, 100, None, XXT)

            else:
                print "invalid divergence!"
                if "gurobi" in divergence:
                    print "gurobi is only valid in single threaded"
                assert(0)
            if isnan(alpha).any():
                alpha = ones(K)/K

        except Exception as inst:
            print type(inst)     # the exception instance
            print inst.args      # arguments stored in .args
            alpha =  ones(K)/K
            it = -1
            dist = -1
            stepsize = -1
            
    end_time = time.time()
    return (v, it, dist, alpha, stepsize, end_time - start_time, gap)

class myIterator:
    def __init__(self,tLambda,T, C, Q, anchors, recoveryLog, divergence, v_max, initial_stepsize, epsilon=10**(-7)):
	self.tLambda =tLambda
	self.T = T
	self.C = C
	self.Q = Q
        self.anchors = anchors
        self.v = -1
        self.V_max = v_max
        self.recoveryLog = recoveryLog
        self.divergence = divergence
        self.X = self.Q[anchors, :]
        if "L2" in divergence:
            self.anchorsTimesAnchors = dot(self.X, self.X.transpose())
        else:
            self.anchorsTimesAnchors = None
        self.initial_stepsize = initial_stepsize
        self.epsilon = epsilon

    def __iter__(self):
        return self
    def next(self):
        self.v += 1
       # print "generating word", self.v, "of", self.V_max
        if self.v >= self.V_max:
            raise StopIteration
            return 0
        v = self.v
        Q = self.Q
	C = self.C
	T= self.T
	tLambda       =   self.tLambda
        anchors = self.anchors
        divergence = self.divergence
        recoveryLog = self.recoveryLog
        return (copy(tLambda), copy(T), copy(C), copy(Q[v, :]), copy(self.X), v, recoveryLog, anchors, divergence, self.anchorsTimesAnchors, self.initial_stepsize, self.epsilon)

#takes a writeable file recoveryLog to log performance
#comment out the recovery log if you don't want it
def nonNegativeRecover(rLambda, T, C, Q, anchors, outfile_name, divergence, max_threads, initial_stepsize=1, epsilon=10**(-7)):

    topic_likelihoodLog = file(outfile_name+".topic_likelihoods", 'w')
    word_likelihoodLog = file(outfile_name+".word_likelihoods", 'w')
    alphaLog = file(outfile_name+".alpha", 'w')

    V = Q.shape[0]
    K = len(anchors)
    A = matrix(zeros((V,K)))
    P_w = matrix(diag(dot(Q, ones(V))))
    for v in xrange(V):
        if isnan(P_w[v,v]):
            P_w[v,v] = 10**(-16)
   	 
    #normalize the rows of Q_prime
    for v in xrange(V):
        Q[v,:] = Q[v,:]/Q[v,:].sum()
   
    s = time.time()
    A = matrix(zeros((V, K)))
    if max_threads > 0:
        pool = multiprocessing.Pool(max_threads)
        print "begin threaded recovery with", max_threads, "processors"
        args = myIterator(rLambda,T, C, Q, anchors, outfile_name+".recoveryLog", divergence, V, initial_stepsize, epsilon)
       	#print "ARGS = ",args
	rows = pool.imap_unordered(fastRecover, args, chunksize = 10)
        for r in rows:
            v, it, obj, alpha, stepsize, t, gap = r
            A[v, :] = alpha
            if v % 1000 == 0:
                print "\t".join([str(x) for x in [v, it, max(alpha)]])
                print >>alphaLog, v, alpha
                alphaLog.flush()
                sys.stdout.flush()
    
    else:
        X = Q[anchors, :]
        XXT = dot(X, X.transpose())
        if divergence == "gurobi_L2":
            scale = 1
            model = Model("distance")
            model.setParam("OutputFlag", 0)
            alpha = [model.addVar() for _ in xrange(K)]
            model.update()
            #sum of c's is 1
            model.addConstr(quicksum(alpha), GRB.EQUAL, 1)
            for k in xrange(K):
                model.addConstr(alpha[k], GRB.GREATER_EQUAL, 0)

            o_static = QuadExpr()
            for i in xrange(K):
                for j in xrange(K):
                    o_static.addTerms(scale*XXT[i,j], alpha[i], alpha[j])

            for w in xrange(V):
                tol = 10**(-16)
                model.setParam("BarConvTol", tol)
                o = QuadExpr()
                o += o_static
                y = Q[w, :]
                XY = dot(X, y)
                YY = float(dot(y, y))
                o += scale*YY
                o += dot(-2*scale*XY, alpha)
                model.setObjective(o, GRB.MINIMIZE)
                model.optimize()
                print "status", model.status
                while not model.status == 2:
                    tol *= 10
                    print "status", model.status, "tol", tol
                    model.setParam("BarConvTol", tol)
                    model.optimize()
                a = array([z.getAttr("x") for z in alpha])
                A[w, :] = a
                print >>alphaLog, w, a
                print "alpha sum is", a.sum()
                print "solving word", w
        
        else:
            for w in xrange(V):
                y = Q[w, :]
                v, it, obj, alpha, stepsize, t, gap= fastRecover((rLambda,T,C,y,X,w,outfile_name+".recoveryLog",anchors,divergence,XXT,initial_stepsize, epsilon))
                A[w, :] = alpha
                if v % 1 == 0:
                    print "word", v, it, "iterations. Gap", gap, "obj", obj, "final stepsize was", stepsize, "took", t, "seconds"
                    print >>alphaLog, v, alpha
                    alphaLog.flush()
                    sys.stdout.flush()

    #rescale A matrix
    #Bayes rule says P(w|z) proportional to P(z|w)P(w)
    originA =copy(A)
    A = P_w * A

    #normalize columns of A. This is the normalization constant P(z)
    colsums = A.sum(0)

    for k in xrange(K):
        A[:, k] = A[:, k]/A[:,k].sum()
    
    A = array(A)

    for k in xrange(K):
        print >>topic_likelihoodLog, colsums[0,k]

    for v in xrange(V):
        print >>word_likelihoodLog, P_w[v,v]
    
    #recoveryLog.close()
    topic_likelihoodLog.close()
    word_likelihoodLog.close()
    return originA, A, colsums

def printToFile(content,file):
	pOutput =open(file,'w')
	pOutput.write(content+'\n')
	pOutput.close()

    
