# enginescala
Rewrite of the workflow engine in Scala/Akka. It will be an extensible barebones. 
All components from its Java counterpart will be present, however the aim is to make it much more resilient and 
scalable, by virtues of Akka, allowing to run more things in parallel, autorecovery etc. 

It it exposing its API as an Akka actor as well as there will be a static API.
