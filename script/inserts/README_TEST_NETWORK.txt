/*
═══════════════════════════════════════════════════════════════════════════
  SHORTEST PATH TEST NETWORK VISUALIZATION
═══════════════════════════════════════════════════════════════════════════

Network Structure:

                    ┌────── MTG (Montagne) [Hotel 2]
                    │       45 km from AIR
                    │
         ┌─── EST ──┘       
         │    25 km
         │    
    ┌─── HUB (15 km) ───┬─── NRD (23/17 km) ───┬─── LAC (Lac) [Hotel 4]
    │     │              │                      │    39/37 km from AIR
    │     │              │                      │
    │     │              └─────────────────┐    │
    │     │                                │    │
    │     └─── SUD (27/19 km) ─── PLG ────┘    │
    │          12 km         (Plage) [Hotel 3]  │
    │                        33 km from AIR     │
    │                                           │
  AIR (10 km)                                   │
    │                                           │
    │                                           │
    └─── CTR (Centre) [Hotel 1] ───────────────┘
         10 km           │    │
                         │    │
                         │    └─── NRD (17 km total)
                         │
                         └─── SUD (19 km total)

═══════════════════════════════════════════════════════════════════════════

OPTIMAL PATHS (Calculated by Dijkstra):

┌────────────────────────────────────────────────────────────────────────┐
│ Destination │ Hotel Name              │ Distance │ Path                │
├─────────────┼─────────────────────────┼──────────┼─────────────────────┤
│ CTR         │ Hotel Centre Palace     │ 10 km    │ AIR → CTR           │
│ NRD         │ Hotel Nord              │ 17 km    │ AIR → CTR → NRD     │
│ SUD         │ Hotel Sud               │ 19 km    │ AIR → CTR → SUD     │
│ EST         │ Villa Est               │ 25 km    │ AIR → HUB → EST     │
│ OUE         │ Residence Ouest         │ 26 km    │ AIR → HUB → OUE     │
│ PLG         │ Hotel de la Plage       │ 33 km    │ AIR → CTR → SUD → PLG│
│ LAC         │ Auberge du Lac          │ 37 km    │ AIR → HUB → OUE → LAC│
│ MTG         │ Resort Montagne         │ 45 km    │ AIR → HUB → EST → MTG│
└─────────────┴─────────────────────────┴──────────┴─────────────────────┘

═══════════════════════════════════════════════════════════════════════════

TEST RESERVATIONS (March 15, 2026):

Time    │ Client   │ Pax │ Hotel                  │ Distance │ Expected Vehicle
────────┼──────────┼─────┼────────────────────────┼──────────┼──────────────────
08:00   │ TEST001  │  8  │ Hotel Centre Palace    │  10 km   │ CAR-8D
08:30   │ TEST002  │ 12  │ Hotel Nord             │  17 km   │ VAN-12D
09:00   │ TEST003  │  5  │ Resort Montagne        │  45 km   │ CAR-5D
09:30   │ TEST004  │ 15  │ Hotel Plage            │  33 km   │ VAN-15E
12:00   │ TEST005  │  4  │ Auberge du Lac         │  37 km   │ CAR-4E
12:30   │ TEST006  │  7  │ Hotel Sud              │  19 km   │ CAR-7E
13:00   │ TEST007  │ 10  │ Villa Est              │  25 km   │ VAN-10E
15:00   │ TEST008  │  3  │ Residence Ouest        │  26 km   │ CAR-4E (reuse)
15:30   │ TEST009  │ 20  │ Hotel Plage            │  33 km   │ VAN-20D
16:00   │ TEST010  │  6  │ Hotel Centre Palace    │  10 km   │ CAR-7E (reuse)
18:00   │ TEST011  │  8  │ Resort Montagne        │  45 km   │ CAR-8D (reuse)
19:00   │ TEST012  │  5  │ Auberge du Lac         │  37 km   │ CAR-5D (reuse)

═══════════════════════════════════════════════════════════════════════════

VEHICLE FLEET:

Reference  │ Capacity │ Fuel Type │ Notes
───────────┼──────────┼───────────┼──────────────────────────────
BUS-50D    │    50    │  Diesel   │ Large bus (backup)
VAN-20D    │    20    │  Diesel   │ Large van
VAN-15E    │    15    │ Electric  │ Electric van
VAN-12D    │    12    │  Diesel   │ Medium van (Diesel preferred)
VAN-10E    │    10    │ Electric  │ Electric medium van
CAR-8D     │     8    │  Diesel   │ Large car (Diesel preferred)
CAR-7E     │     7    │ Electric  │ Electric car
CAR-5D     │     5    │  Diesel   │ Standard car (Diesel preferred)
CAR-4E     │     4    │ Electric  │ Small electric car

═══════════════════════════════════════════════════════════════════════════

VERIFICATION CHECKLIST:

✓ Shortest path algorithm correctly finds multi-hop routes
✓ Best-fit vehicle selection (smallest that fits)
✓ Diesel preference when same capacity
✓ Time overlap detection for vehicle availability
✓ Vehicle reuse after trip completion
✓ Complex network with 3+ hop paths

═══════════════════════════════════════════════════════════════════════════

TO TEST:
1. Run: script/inserts/test_shortest_path.sql
2. Navigate to: /assignation
3. Select date: 2026-03-15
4. Verify assignments match expected vehicles
5. Check calculated distances match the table above

═══════════════════════════════════════════════════════════════════════════
*/
