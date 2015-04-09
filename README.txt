Goals: 
- design protocol to adopt AODV (RFC 3561) to delay-tolerant networks
- implement designed algorithm

Content:
- src - folder with sources
- 1 research - algorithm presentation 
- 2 model implementation - model design
- 3 model testing - modeling
- 4 listing
- simulator.jar - model simulator


Description

Delay-tolerant routing protocol based on AODV (RFC 3561). Designed for Ad-Hoc networks with DTN/MANET architectures. Also may be used in overlay networks for distributed computing.
Unlike AODV, it processes the exct break in the middle of chain, instead of dicarding the whole path. This allows very fast route repairment by finding alternative chains near from node, which detects break. This trick is effective in massive Mesh-networks, when chains with optimal metric are existing with high possibility.
Also, there is the experimental part, which uses logical coordinates for authomaticaly finding (without broadcast) alternative nodes or even moved destination. This is effective if many intermediate nodes was moved during detect-repair phase; however it's applicable only for intervals between route bends.

Benefits

  -  fastest reaction on topology changes (especially for massive Mesh)
  -  not restricted route length (on maintenance phase)
  -  less channel load.
