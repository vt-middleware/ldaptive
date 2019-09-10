#//# --------------------------------------------------------------------------------------
#//# Created using Sequence Diagram for Mac
#//# https://www.macsequencediagram.com
#//# https://itunes.apple.com/gb/app/sequence-diagram/id1195426709?mt=12
#//# --------------------------------------------------------------------------------------
# Produced with Sequence Diagram
# http://macsequencediagram.com/

title "Default Connection Strategy Initialization"

participant Client as C
participant DefaultConnectionFactory as DCF
participant LdapURLSet as LUS
participant ConnectionStrategy as CS
participant NettyProvider as NP
participant NettyConnection as NC

activate C
activate DCF
C-->DCF: setConnectionConfig(cc)
activate LUS
DCF-->LUS: new(cc.getConnectionStrategy(), cc.getLdapUrl())
activate CS
LUS-->CS: populate(ldapUrls, this)
C-->DCF: getConnection()
activate NP
DCF-->NP: create(this)
activate NC
NP-->NC: new(EventLoopGroup, ConnectionFactory)
NC-->LUS: getActiveURLs()
NC--> NC: schedule URL checker tasks
C-->NC: open()
loop
  NC-->LUS: doWithNextActiveUrl(openerConsumer)
  alt [Success]
    NC-->NC: open = true
    NC-->C: return to caller
  else [Failure]
    NC-->NC: recordFailure()
    alt [Has More Active URLs]
      NC-->NC: try next URL
    else [Exhausted Available URLs]
      NC-->C: raise exception
    end
  end
end