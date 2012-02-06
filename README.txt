Goals: 
- adapting AODV to Delay-Tolerant Networking
- implementation of result alghorithm

Content:
- src - folder with sources
- 1 research - algorithm presentation 
- 2 model implementation - model design
- 3 model testing - modeling
- 4 listing
- simulator.jar - model simulator


Description

Delay-tolerant routing protocol partially based on AODV (RFC 3561). Designed for Ad-Hoc networks with DTN/MANET architectures. Also may be used in overlay Internet-networks for distributed computing.
In diff from AODV detects when moved only intermediate chain, not destination. This allows very fast route repair without any additional overhead (by finding replacing chains near from node, that detects break). This trick is effective in massive Mesh-networks, when chains with optimal metric will be present with high possibility.
Also, have an experimental part, that uses logical coordinates for authomaticaly finding (without broadcast) replacing nodes or even moved destination. This effectively when many intermediate nodes was moved during detect-repair, but may be used only on intervals between route bends.

Results

  -  fastest reaction on topology changes (especially for massive Mesh)
  -  not restricted route length (on maintenance phase)
  -  less channel load.
