Challenge: 
- design AODV-like protocol (RFC 3561) aimed at massive delay-tolerant networks

Content:
- src - folder with sources
- 1 research - algorithm presentation 
- 2 model implementation - model design
- 3 model testing - modeling
- 4 listing
- simulator.jar - model simulator


Description

Delay-tolerant routing protocol based on AODV (RFC 3561). Designed for Ad-Hoc networks with DTN/MANET architectures and also overlay networks in distributed computing.
Unlike AODV, it precicely deals with exact connection break in the middle of chain, instead of dicarding the whole path. This allows very fast route repairment through finding alternative chains nearby. This trick is effective in massive Mesh-networks, where it's more likely that such repaired chain has optimal metric (as optimal as of fully rediscovered path) with high probability.
There is an experimental extension, which uses logical coordinates to automaticaly find (without broadcast) alternative nodes or even moved destination. This is effective if many intermediate nodes were moved during detect-repair phase; however it's applicable only for intervals between route bends.

Benefits

  -  fastest reaction to topology changes (especially for massive Mesh)
  -  path length is not restricted  (on maintenance phase)
  -  less channel load.
