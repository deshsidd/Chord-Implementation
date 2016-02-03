import akka.actor._
import scala.util.Random
import scala.collection.mutable.StringBuilder
import scala.util.Random
import scala.concurrent.duration._
import scala.math
import scala.runtime.RichInt    
import java.util.concurrent.TimeUnit
import scala.io.Source
import chordLib.Utilities  // getId : BigInt    //getFile : String     // sha1 : String  109583
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap
import scalaxy.loops._


//INITIALIZING MESSAGES
case class findSucc(nodeId: BigInt , senderId: BigInt , numOfHops:Int , operation:String , tempnode:BigInt , index:Int , str:String)
case class succFound(nodeId: BigInt , succId:BigInt , numOfHops:Int , operation:String , tempnode:BigInt , index:Int , str:String)
case class fixFingers()
case class sendRequest()
case class printFingerTable()
case class operationComplete(avgNumOfHops: Int)
case class shutdown()
case class stabalize(pred: BigInt)
case class sendPred(nodeId: BigInt)
case class stabalizeTick()
case class Notify(nodeId: BigInt)
case class join(nodeId:BigInt)
case class findPred(nodeId: BigInt , senderId: BigInt , numOfHops:Int , operation:String , tempnode:Int, index:Int)
case class predFound(nodeId: BigInt , predId:BigInt , numOfHops:Int , operation:String , tempnode:Int, index:Int)
case class initFingerTable(nodeId: BigInt , succId: BigInt) 
case class updateOthers(nodeId: BigInt)
case class updateFingerTable(nodeId: BigInt , index:Int)         
case class yourFingerTable(arr:Array[BigInt])    
case class nodeJoining()
case class createNode(nodeId:BigInt, succId:BigInt, predId:BigInt)
case class nodeCreated(nodeId:BigInt, succId:BigInt, predId:BigInt)
case class insertWord(wordId:BigInt, word:String)
case class yourWord(wordId:BigInt, word:String)
case class search(word:String)
case class findWord(word:String)
case class wordResult(wordId:BigInt,word:String,nodeId:BigInt,status:Boolean)
case class checkForWord(wordId:BigInt,word:String)

               

object chord
{
    //INITIALIZING GLOBAL VARIABLES
    import system.dispatcher //used for scheduler
	var Lib = new Utilities //library that contains functions
	val system = ActorSystem("ChordSystem") 
    var nodes = new ArrayBuffer[BigInt]()
    var Words= new ArrayBuffer[String]()
    val m=160 // bits in sha 2^m
    var numNodes=0
    var numRequests=0
    var numJoins=0
    var choice=""
    var dictChoice=""
    var numWords=0
    var dict=false

///////////////////
//////////////////
//////MAIN///////
/////////////////
/////////////////
    

     def main(args: Array[String]): Unit = 
    {	  

             //TAKING INPUT
             
    	     numNodes = (readLine("Enter the number of nodes : ")).toInt
             
             dictChoice=readLine("Do you want to search for words in the distributed dictionary?(Enter 'yes' or 'no')")
             if(dictChoice=="no" || dictChoice=="NO")
             {
                numJoins= (readLine("Enter the number of new nodes you want to join chord : ")).toInt
                numRequests = (readLine("Enter the number of requests each node has to make: ")).toInt
                


             }
             else
             {
                dict=true
                numWords = (readLine("Enter the number of words: ")).toInt

                for( i <- 0 to numWords-1 optimized) 
                {
                    Words+=readLine("Enter Word Number " + (i+1)+ ": ").toLowerCase()


                }
             }
             
             
            println("The Chord Network will take about 20 seconds to stabalize and update finger tables of the nodes.")
            choice=readLine("Do you want to wait for network to stabalize? (Enter 'yes' or 'no') \n NOTE: choosing 'no' can affect performace of the network initially : ")



            val Creator = system.actorOf(Props(new Creator(numNodes,numRequests,numJoins)), name = "Creator")
    }

///////////////////
//////////////////
//////CREATOR/////
/////////////////
/////////////////



    class Creator(numNode:Int, numRequests:Int, numJoins:Int) extends Actor
    {

            //INITIALIZING COUNTERS, VARIABLES AND ARRAYS
            var TotalAvgHops=new ArrayBuffer[Int]()
            var average=0
    	    var counter=0
            var operationCounter=0
            numNodes=numNode
            var numberNodes=numNodes // this one includes the nodes that join
            var joinCounter=0
            var wordCounter=0
    		for( i <- 1 to numNodes optimized)  // CREATING NODE IDS
    		{
    			nodes+=Lib.getId("node"+ i)
    			println("node " + i + " is" + nodes(i-1))

    			
    		}
    		nodes=nodes.sortWith(_ < _) // SORTING NODES
    		
            for( i <- 0 to nodes.length-1 optimized)  // INITIALIZING THE TOPOLOGY
            {
            	if(i==0)
            	{
            		val Node = context.actorOf(Props(new Node(nodes(i),nodes(i+1),nodes(nodes.length-1))) , name="Node" + nodes(i))
	                println("Node " + (i+1) + " created with id: Node" + nodes(i))

            	}
            	else if(i==nodes.length-1)
            	{
            		val Node = context.actorOf(Props(new Node(nodes(i),nodes(0),nodes(i-1))) , name="Node" + nodes(i))
	                println("Node " + (i+1) + " created with id: Node" + nodes(i))

            	}
            	else
            	{
            		val Node = context.actorOf(Props(new Node(nodes(i),nodes(i+1),nodes(i-1))) , name="Node" + nodes(i))
	                println("Node " + (i+1) + " created with id: Node" + nodes(i))

            	}
	            
            }
            println("processing.. please wait....")
            if(dict)
            {
                println("Creating Dictionary.....")
                for( i <- 0 to 19999 optimized)
                 {
                    var word=Lib.getFile(i)
                    var wordId=Lib.getId(word)
                    var rand=Random.nextInt(numNodes)
                    val chosenNode = system.actorSelection("/user/Creator/Node" + nodes(rand))

                    chosenNode ! insertWord(wordId,word)


                     
                 }
                 println("Distributed Dictionary Created.")
                 println("Now searching for words...")
                 for(  i<- 0 to numWords-1 optimized) 
                 {
                    val dictTicker =system.scheduler.scheduleOnce(30000 milliseconds,self,search(Words(i)))
                     
                 }
                 
                 
            }
            
            for( i <- 1 to numJoins optimized)
            {
                 val joinTicker =system.scheduler.scheduleOnce(15000 milliseconds,self,nodeJoining())
                
            }
           

            //val requestTicker =system.scheduler.schedule(20000 milliseconds,1000 milliseconds,self,sendRequest())


            def receive =
            {
                case sendRequest() =>   // 965604837178937065172715823946565609171909472919   <- hash for siddhant
                                        counter+=1
                                        if(counter <= numRequests)
                                        {
                                            var rand=Random.nextInt(numNodes)
                                            //println("random number chosen is " + rand)
                                            //var id:BigInt = nodes(4)//
                                            val chosenNode = system.actorSelection("/user/Creator/Node" + nodes(rand))
                                            //println("id for siddhant :" + Lib.getId("siddhant"))
                                            chosenNode ! findSucc(Lib.getId("siddhant"),nodes(rand),0,"search",0,0,"")
                                        }
                                        else
                                        {

                                            //requestTicker.cancel()
                                            for( i <- 0 to numNodes-1 optimized)  // creating ids of the nodes
                                            {
                                                if(i==1)//
                                                {
                                                    var node=system.actorFor("/user/Creator/Node"+ nodes(i))
                                                    //system.scheduler.scheduleOnce(((i+1)*1000) milliseconds, node, printFingerTable())
                                               }
                                            
                                            }
                                            
                                        }

                case operationComplete(avgNumOfHops) => TotalAvgHops.append(avgNumOfHops)
                                                        operationCounter+=1
                                                        //println("in creator at operation complete. counter :" + operationCounter)
                                                        if(operationCounter == (numNodes+numJoins))
                                                        {
                                                            average=(TotalAvgHops.sum)/numberNodes
                                                            println("operation complete with "+numberNodes+" nodes. Average hops per request is :" +average)
                                                            for( i <- 0 to numberNodes-1 optimized)  // creating ids of the nodes
                                                            {
                                                                //if(i==nodes.length-1)//
                                                                {
                                                                    //var node=system.actorFor("/user/Creator/Node"+ nodes(i))
                                                                    //system.scheduler.scheduleOnce((1000*i) milliseconds, node, printFingerTable())
                                                               }
                                                            
                                                            }
                                                            system.scheduler.scheduleOnce(0 milliseconds, self, shutdown()) 
                                                        }

                case nodeJoining() => 


                                      
                                      numberNodes=numberNodes+1
                                      var nodeId=Lib.getId("node"+ numberNodes)
                                      //println("numberNodes is :" + numberNodes + " and node id is "+ nodeId)
                                      nodes+=nodeId
                                      var rand=Random.nextInt(numNodes)
                                      var chosenNode=system.actorFor("/user/Creator/Node"+ nodes(rand))
                                      chosenNode ! join(nodeId)
                                     

                case createNode(nodeId,succId,predId) => joinCounter+=1
                                                         val Node = context.actorOf(Props(new Node(nodeId,succId,predId)) , name="Node" + nodeId)
                                                         println("new node " + joinCounter +" created with id" + nodeId + "\n")
                                                         sender ! nodeCreated(nodeId,succId,predId)

                case search(word) => var rand=Random.nextInt(numNodes)
                                      var chosenNode=system.actorFor("/user/Creator/Node"+ nodes(rand))
                                      chosenNode ! findWord(word)

                case wordResult(wordId,word,nodeId,status) =>   if(status)
                                                                {
                                                                    println("word "+ word + " found with word id " + wordId + "at node : "+nodeId)

                                                                }
                                                                else
                                                                {
                                                                    println("word "+ word + " not found with word id " + wordId + "at node : "+nodeId)
                                                                }
                                                                wordCounter+=1
                                                                if(wordCounter==numWords)
                                                                {
                                                                    println("operation successfully completed.")
                                                                    self ! shutdown()
                                                                }





                case shutdown() =>  context.system.shutdown()


            	case _ => println("Creator Received Something Fishy...")

            }

    }



///////////////////
//////////////////
//////NODE///////
/////////////////
/////////////////



    class Node(id:BigInt , suc:BigInt , pre:BigInt) extends Actor
    {
        val WordMap = new HashMap[BigInt,String]()
        var NodeHops = new ArrayBuffer[Int]() // stores the number of hops for each request per node
        var avgNumOfHops=0 
        var FingerTable = Array.ofDim[BigInt](m)
        val two:BigInt=2
        var fixCounter=m // counter for fixing fingers
        var counter=0 
        var requestCounter=0 // counter for counting number of requests sent
        var succ=suc
        var pred=pre
        var arr=new Array[BigInt](m) // array for initializing new nodes finger table
        var initCounter=0 // initialzing new node finger table counter
        createFingerTable() // INITIALIAZE THE FINGER TABLES. (CONTAIN ONLY FIRST ENTRY- SUCCESSOR)

    	def createFingerTable()
        {
            for( i <- 1 to m-1 optimized) 
            {
                FingerTable(i)=0

                
            }
            FingerTable(0)=succ

        }
        //HELPER FUNCTIONS
        def Start(index: Int):BigInt=
        {
            var s=(id + two.pow(index)).mod(two.pow(m))
            return s
        }

        def End(index: Int):BigInt=
        {
            var e:BigInt=0
            if(index==m-1)
            {
                return id
            }
            else
            {
                var e=(id + two.pow(index+1)).mod(two.pow(m)) 
                return e
            }
            return e
        }

        def Finger(index:Int):BigInt=
        {
            FingerTable(index)
        }

        



        def liesBetween1(tempId:BigInt,a:BigInt , b: BigInt):Boolean=
        {
            if(a < b)
            {
                if(tempId >= a && tempId < b)
                {
                    return true
                }
                else
                {
                    return false
                }
            }

            else
            {
                if(  (tempId >= a  &&   tempId <= two.pow(160))   ||  (tempId >= 0  && tempId < b)   )
                {
                    return true
                }
                else
                {
                    return false
                }
            }
 


        }

        def liesBetween2(tempId:BigInt,a:BigInt , b: BigInt):Boolean=
        {
            if(a < b)
            {
           
                 if(tempId > a && tempId <= b)
                {
                  
                    return true
                }
                else
                {
                   
                    return false
                }
            }
            else
            {
                if(  (tempId > a  &&   tempId <= two.pow(160))   ||  (tempId >= 0  && tempId <= b)   )
                {
                    return true
                }
                else
                {
                    return false
                }
            }
        

        }

        def liesBetween(tempId:BigInt,a:BigInt , b: BigInt):Boolean=
        {
            if(a < b)
            {


                 if(tempId > a && tempId < b)
                {

                    return true
                }
                else
                {
                  
                    return false
                }
            }
            else
            {
                if(  (tempId > a  &&   tempId <= two.pow(160))   ||  (tempId >= 0  && tempId < b)   )
                {
                    return true
                }
                else
                {
                    return false
                }
            }
        

        }

          def liesBetween12(tempId:BigInt,a:BigInt , b: BigInt):Boolean=
        {
            if(a <= b)
            {


                 if(tempId >= a && tempId <= b)
                {

                    return true
                }
                else
                {
                  
                    return false
                }
            }
            else
            {
                if(  (tempId >= a)   ||  (tempId <= b)   )
                {
                    return true
                }
                else
                {
                    return false
                }
            }
        

        }


        def closestPrecedingNode(nodeId:BigInt):BigInt=
        {
            var i=m-1
            while(i>=0)
            {
                if(liesBetween2(Finger(i),id ,nodeId))
                {
                    if(Finger(i)!=0)
                    {       
                    return Finger(i)
                    }
                    
                }
                i=i-1

                
            }
            //println("should not have happened")
            return succ
            



        }

        def fixFinger(nodeId:BigInt , index:Int , succId:BigInt)
        {
            //println("Finger " + index + " updated for node " + id)
            FingerTable(index)=succId
        }


        def checkWordMap(wordId:BigInt, word:String):Boolean=
        {
            var b:Boolean=WordMap.contains(wordId)
            b
        }


        
        //INITIALIZING TICKERS
    		
        private var requestTicker: Cancellable = _
        private var stabalizeTicker: Cancellable=_
        
        private val fixFingersTicker =system.scheduler.schedule(0 milliseconds,50 milliseconds,self,fixFingers())

        if(choice=="yes" || choice=="YES")
        {
              if(dict)
              {
                  stabalizeTicker =system.scheduler.schedule(10000 milliseconds,1200 milliseconds,self,stabalizeTick())
              }
              else
              {

                  requestTicker =system.scheduler.schedule(30000 milliseconds,1000 milliseconds,self,sendRequest())
                  stabalizeTicker =system.scheduler.schedule(10000 milliseconds,1200 milliseconds,self,stabalizeTick())
              }
        }
        else
        {
             if(dict)
             {
                 stabalizeTicker =system.scheduler.schedule(0 milliseconds,1200 milliseconds,self,stabalizeTick())
             }
             else
             {
                 requestTicker =system.scheduler.schedule(0 milliseconds,1000 milliseconds,self,sendRequest())
                 stabalizeTicker =system.scheduler.schedule(0 milliseconds,1200 milliseconds,self,stabalizeTick())
             }
        }

        


    		def receive =
            {
                    ///////////////////
                    //////////////////
                    //GET PRED AND ///
                    //   SUCC     ///
                    /////////////////
                case findSucc(nodeId , senderId , numOfHops , operation , tempnode , index , str) =>  //println("in succ at node" + id)
                            //succ of //sender   // hops  // string  //node id field // index                                                                         
                                                                                                var numHops=numOfHops
                                                                                                numHops+=1
                                                                                                if(liesBetween2(nodeId,pred,id))
                                                                                                {
                                                                                                    if(operation=="search")
                                                                                                    {
                                                                                                        //println("found at node " + id)
                                                                                                    }
                                                                                                    var sender= system.actorSelection("/user/Creator/Node"+senderId)
                                                                                                    sender ! succFound(nodeId, id ,numHops,operation,tempnode , index ,str)
                                                                                                }
                                                                                                else if(liesBetween2(nodeId,id,succ))
                                                                                                {   
                                                                                                    if(operation=="search")
                                                                                                    {
                                                                                                        //println("found at node " + id)
                                                                                                    }
                                                                                                    //println("in true at node " + id)
                                                                                                    //println("sender id is :" + senderId)       
                                                                                                    var sender= system.actorSelection("/user/Creator/Node"+senderId)
                                                                                                    sender ! succFound(nodeId, succ,numHops,operation,tempnode,index , str)
                                                                                                }
                                                                                                else
                                                                                                {

                                                                                                    //println("in false at node " + id)
                                                                                                    //println("succ id is :" + succ)
                                                                                                    var chosenNode=closestPrecedingNode(nodeId)
                                                                                                    var chosenOne=system.actorSelection("/user/Creator/Node"+chosenNode)
                                                                                                    if(operation=="search")
                                                                                                    {
                                                                                                        //println("not found.. send ing request to node" + chosenNode)
                                                                                                    }
                                                                                                     chosenOne ! findSucc(nodeId,senderId,numHops,operation,tempnode,index,str)
                                                                                                }

                case succFound(nodeId, succId ,numOfHops,operation,tempnode,index , str) =>  operation match 
                                                                                        {
                                                                                            case "search" => NodeHops.append(numOfHops)
                                                                                                             counter+=1
                                                                                                             if(counter <= numRequests)
                                                                                                             {
                                                                                                                 println("Word : " + str +" with id : "+ nodeId+ " is at Node : "+ succId + "\nnumber of hops : " + numOfHops )
                                                                                                             }
                                                                                                             //println("at node "+ id)
                                                                                                             if(counter > numRequests-1)
                                                                                                             {
                                                                                                                avgNumOfHops= (NodeHops.sum)/numRequests
                                                                                                                //println("average number of hops for node :"+id+ " is " +avgNumOfHops)
                                                                                                                var master=system.actorSelection("/user/Creator")
                                                                                                                master ! operationComplete(avgNumOfHops)
                                                                                                                
                                                                                                             }

                                                                                            case "fix" => fixFinger( nodeId, index , succId)

                                                                                            case "newnode" =>  var master=system.actorSelection("/user/Creator")
                                                                                                                master ! createNode(nodeId,succId,0)
                                                                                                                //self ! updateFingerTable(nodeId,index)
                                                                                                                
                                                                                                              
                                                                                                              
                                                                                            case "init" => initCounter+=1
                                                                                                            //println("in init index" + initCounter)
                                                                                                           arr(index)=succId
                                                                                                           if(initCounter >= m)
                                                                                                           {
                                                                                                                //println("in if")
                                                                                                                initCounter=0
                        
                                                                                                                var newnode=system.actorSelection("/user/Creator/Node"+tempnode)
                                                                                                                newnode ! yourFingerTable(arr)
                                                                                                           }

                                                                                            case "word" => var node=system.actorSelection("/user/Creator/Node"+succId)
                                                                                                             node ! yourWord(nodeId,str)

                                                                                            case "wordsearch" => 
                                                                                                                 var node=system.actorSelection("/user/Creator/Node"+succId)
                                                                                                                 node ! checkForWord(nodeId,str)
                                                                                                                 

                                                                                        }






                                                                                


            case findPred(nodeId , senderId , numOfHops , operation , tempnode ,index) =>  //println("in succ at node" + id)
                                                                                            var numHops=numOfHops
                                                                                            numHops+=1
                                                                                            if(liesBetween2(nodeId,pred,id))
                                                                                            {
                                                                                                if(operation=="search")
                                                                                                {
                                                                                                    //println("found at node " + id)
                                                                                                }
                                                                                                var sender= system.actorSelection("/user/Creator/Node"+senderId)
                                                                                                sender ! predFound(nodeId, pred ,numHops,operation,tempnode,index)
                                                                                            }
                                                                                            else if(liesBetween2(nodeId,id,succ))
                                                                                            {   
                                                                                                if(operation=="search")
                                                                                                {
                                                                                                    //println("found at node " + id)
                                                                                                }
                                                                                                      
                                                                                                var sender= system.actorSelection("/user/Creator/Node"+senderId)
                                                                                                sender ! predFound(nodeId, id,numHops,operation,tempnode,index)
                                                                                            }
                                                                                            else
                                                                                            {

                                                                                                var chosenNode=closestPrecedingNode(nodeId)
                                                                                                var chosenOne=system.actorSelection("/user/Creator/Node"+chosenNode)
                                                                                                if(operation=="search")
                                                                                                {
                                                                                                    //println("not found.. send ing request to node" + chosenNode)
                                                                                                }
                                                                                                 chosenOne ! findPred(nodeId,senderId,numHops,operation,tempnode,index)
                                                                                            }

                case predFound(nodeId, predId ,numOfHops,operation,tempnode,index) =>  operation match 
                                                                                        {
                                                                                            case "search" => NodeHops(counter)=numOfHops
                                                                                                             counter+=1
                                                                                                             println("predecessor for "+ nodeId+ " is "+ predId + "\nnumber of hops :" + numOfHops )

                                                                                            case "update" => var node=system.actorSelection("/user/Creator/Node"+predId)
                                                                                                             node ! updateFingerTable(nodeId,index)
                                                                                                            


                                                                                            






                                                                                        }

                    ///////////////////
                    //////////////////
                    //STABALIZE AND//
                    ///   NOTIFY   //
                    /////////////////


                 case stabalizeTick() => var s=system.actorSelection("/user/Creator/Node"+succ)
                                         s ! sendPred(id)


                 case sendPred(nodeId) => var sender=system.actorSelection("/user/Creator/Node"+nodeId)
                                          sender ! stabalize(pred)
                                          //println("pred requested from node "+ id)

                 case stabalize(p) => if(liesBetween(p,id,succ))
                                      {
                                            succ=p
                                            var newnode=system.actorSelection("/user/Creator/Node"+p)
                                            newnode ! Notify(id)
                                            //println("successor of node" + id + "was changed to " + succ+ "\n")

                                      }

                 case Notify(nodeId) => if(pred==0 || liesBetween(nodeId,pred,id))
                                        {
                                            pred=nodeId
                                            //println("predecessor of node "+ id +"was changed to " + pred+ "\n")
                                        }

                 
                    /////////////////
                    ////////////////
                    //NODE JOIN ////
                    ////////////////
                    ///////////////


                case join(nodeId) =>  self ! findSucc(nodeId , id , 0 , "newnode" , 0 ,0 , "")


                case nodeCreated(nodeId,succId,predId) => 
                                                            var s=system.actorSelection("/user/Creator/Node"+succId)
                                                            s ! Notify(nodeId)
                                                            self ! initFingerTable(nodeId , succId)
                                                            self ! updateOthers(nodeId)
                                                    

                

                

                 case initFingerTable(nodeId,succId) => var s:BigInt=0
                                                      
                                                        for( i <- 0 to m-1 optimized) 
                                                      {
                                                            
                                                               
                                                                s=(nodeId + two.pow(i)).mod(two.pow(m))
                                                                self ! findSucc(s,id,0, "init" ,nodeId,i,"")
                                                                                           
                                                      }


                  case yourFingerTable(arr) => for( i <- 0 to m-1 optimized) 
                                             {
                                                    fixFinger(id,i,arr(i))
                                                 
                                             }
                                             println("finger table of new node : " + id + " updated successfully\n")


                 case updateOthers(nodeId) =>   var p:BigInt=0
                                                for( i <- 0 to m-1 optimized) 
                                                 {
                                                     p=(nodeId-two.pow(i)).mod(two.pow(m))
                                                     var node=system.actorSelection("/user/Creator/Node"+p)
                                                     node ! findPred(p,id,0,"update",0,i)

                                                 }

                 case updateFingerTable(nodeId,index) => if(liesBetween1(nodeId,id,Finger(index)))
                                                         {
                                                                fixFinger(id,index,nodeId)
                                                                println("finger table entry num :" +index+ "for node "+ id +" was updated to " + nodeId+ "\n")
                                                                if(pred!=0)
                                                                {
                                                                    var node=system.actorSelection("/user/Creator/Node"+pred)
                                                                    node ! updateFingerTable(nodeId,index)
                                                                }


                                                         }

                
                                                                                                                                  
                                                                                               
                 
                    /////////////////
                    ////////////////
                    //     REST   //
                    ////////////////
                    ///////////////
                                    
                 case sendRequest() =>   
                                        requestCounter+=1
                                        if(requestCounter <= numRequests)
                                        {
                                            //println("sent request to node "+ id)
                                            var rand=Random.nextInt(20000)
                                            var word=Lib.getFile(rand)
                                            var wordId=Lib.getId(word)
                                            
                                            self ! findSucc(wordId,id,0,"search",0,0,word)
                                        }
                                        else
                                        {

                                            requestTicker.cancel()
                                            
                                            
                                            
                                        }


                 case fixFingers() =>  //println("fixing fingers for node " + id)
                                     if(fixCounter==0)
                                     {
                                        fixCounter=m
                                     }
                                     fixCounter-=1
                                     
                                     self ! findSucc(Start(fixCounter),id,0, "fix" , 0 ,fixCounter,"")


                 case printFingerTable() =>  //println("PRINTING FINGER TABLE FOR NODE " + id)
                                            println("\n\n\n")
                                            for( i <- 0 to m-1 optimized) 
                                            {

                                                println( Start(i) + "  ,  " + End(i) + "  ,  " + Finger(i))
                                                
                                            }
                   /////////////////
                    ////////////////
                    //DICTIONARY ///
                    ////////////////
                    ///////////////


                case insertWord(wordId,word) =>          self ! findSucc(wordId,id,0, "word" , 0 ,0,word)

                case yourWord(wordId,word) => WordMap+=wordId -> word
                                              //println("word :"+word+ " with id "+wordId+ " inserted at node "+ id)

                case findWord(word) => var wordId=Lib.getId(word)

                                       self ! findSucc(wordId,id,0,"wordsearch",0,0,word)

                case checkForWord(wordId,word) => var result=checkWordMap(wordId,word)
                                                    var master=system.actorSelection("/user/Creator")
                                                    if(result)
                                                    {

                                                        master ! wordResult(wordId,word,id,true)
                                                                                                                        



                                                         }
                                                 else
                                                    {
                                                         master ! wordResult(wordId,word,id,false)

                                                    }


                                       








            	case _ => println("Node" + id + " Received Something Fishy...")


                   





            }






























    }














    


    
  
}




