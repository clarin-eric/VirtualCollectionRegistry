package eu.clarin.cmdi.virtualcollectionregistry.oai.impl;

import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArraySet;

public final class IPFilter {
    public static class Subnet {
        private final int network;
        private final int netmask;

        public Subnet(String subnet) {
            if (subnet == null) {
                throw new NullPointerException("subnet == null");
            }
            int pos = subnet.indexOf('/');
            if (pos != -1) {
                int addr = parseQuattedDot(subnet.substring(0, pos));
                netmask = parseCIDRNetmask(subnet.substring(pos + 1));
                network = addr & netmask;
            } else {
                network = parseQuattedDot(subnet);
                netmask = 0xFFFFFFFF;
            }
        }

        public Subnet(String address, String mask) {
            if (address == null) {
                throw new NullPointerException("address == null");
            }
            if (mask == null) {
                throw new NullPointerException("mask == null");
            }
            int addr = parseQuattedDot(address);
            netmask = parseQuattedDot(mask);
            network = addr & netmask;
        }

        @Override
        public boolean equals(Object other) {
            if ((other != null) && (other instanceof Subnet)) {
                final Subnet o = (Subnet) other;
                return (this.network == o.network) &&
                        (this.netmask == o.netmask);
            }
            return false;
        }

        public boolean isInRange(String address) {
            final int addr = parseQuattedDot(address) & netmask;
            return (isInRange(addr));
        }

        public boolean isInRange(int address) {
            return ((address & netmask) == network);
        }

        public String getNetwork() {
            return buildQuattedDot(network, 0xFFFFFFFF,
                    new StringBuilder()).toString();
        }

        public String getNetmask() {
            return buildQuattedDot(netmask, 0xFFFFFFFF,
                    new StringBuilder()).toString();
        }

        public String getCIDR() {
            return buildCIDR(network, netmask,
                    new StringBuilder()).toString();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(super.toString());
            sb.append("[net=");
            buildCIDR(network, netmask, sb);
            sb.append("]");
            return sb.toString();
        }

        static int parseQuattedDot(String input) {
            int addr = 0;
            int value;
            int start = 0;
            int end;
            int digitCount = 0;

            while ((end = input.indexOf('.', start)) != -1) {
                end = input.indexOf('.', start);
                try {
                    value = Integer.parseInt(input.substring(start, end));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("invalid number: " +
                            input.substring(start, end));
                }
                if ((value < 0) || (value > 255)) {
                    throw new IllegalArgumentException("invalid number: " +
                            value);
                }
                addr |= value;
                addr <<= 8;
                start = end + 1;
                if (++digitCount > 3) {
                    throw new IllegalArgumentException("invalid addr: " +
                            input);
                }
            }
            try {
                value = Integer.parseInt(input.substring(start));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("invalid number: " +
                        input.substring(start));
            }
            if ((value < 0) || (value > 255)) {
                throw new IllegalArgumentException("invalid number: " + value);
            }
            addr = addr | value;
            // fill remaining bits with zeros
            while (++digitCount < 4) {
                addr <<= 8;
            }
            return addr;
        }

        private static int parseCIDRNetmask(String input) {
            try {
                int bits = Integer.parseInt(input);
                if ((bits < 0) || (bits > 32)) {
                    throw new IllegalArgumentException("invalid netmask: " +
                            bits);
                }
                int mask = 0;
                for (int i = 0; i < 31; i++) {
                    if (bits-- > 0) {
                        mask |= 1;
                    }
                    mask <<= 1;
                }
                if (bits > 0) {
                    mask |= 1;
                }
                return mask;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("invalid netmask: " + input);
            }
        }

        private static StringBuilder buildQuattedDot(int addr, int netmask,
                StringBuilder sb) {
            int mask = 0xFF000000;
            int digits = Integer.numberOfTrailingZeros(netmask) / 8;
            for (int i = 4; i > digits; i--) {
                int value = (addr & mask) >> ((i - 1) * 8) & 0x000000FF;
                sb.append(Integer.toString(value));
                if (i > (digits + 1)) {
                    sb.append('.');
                }
                mask >>= 8;
            }
            return sb;
        }

        private static StringBuilder buildCIDR(int addr, int netmask,
                StringBuilder sb) {
            int len = sb.length();
            buildQuattedDot(addr, netmask, sb);
            if (len == sb.length()) {
                sb.append('0');
            }
            sb.append('/');
            sb.append(32 - Integer.numberOfTrailingZeros(netmask));
            return sb;
        }

    } // inner class Subnet

    public static class List implements Iterable<Subnet> {
        private final CopyOnWriteArraySet<Subnet> entries =
            new CopyOnWriteArraySet<Subnet>();

        private List() {
        }

        public void add(Subnet subnet) {
            if (subnet != null) {
                entries.add(subnet);
            }
        }

        public void add(String addresses) {
            if (addresses == null) {
                throw new NullPointerException("addresses == null");
            }
            String[] tokens = addresses.split("[\\s,]+");
            for (String token : tokens) {
                if (token.trim().length() > 0) {
                    add(new Subnet(token));
                }
            }
        }

        public void remove(Subnet subnet) {
            if (subnet != null) {
                entries.remove(subnet);
            }
        }

        public void remove(String subnet) {
            remove(new Subnet(subnet));
        }

        public boolean isInList(String address) {
            return isInList(Subnet.parseQuattedDot(address));
        }

        public Iterator<Subnet> iterator() {
            return entries.iterator();
        }

        private boolean isInList(int address) {
            if (!entries.isEmpty()) {
                for (Subnet entry : entries) {
                    if (entry.isInRange(address)) {
                        return true;
                    }
                }
            }
            return false;
        }

    } // inner class List

    private final List whitelist = new List();
    private final List blacklist = new List();

    IPFilter() {
    }

    public List getWhitelist() {
        return whitelist;
    }

    public List getBlacklist() {
        return blacklist;
    }

    public void addToWhitelist(String addresses) {
        if (addresses != null) {
            whitelist.add(addresses);
        }
    }

    public void addToBlacklist(String addresses) {
        if (addresses != null) {
            blacklist.add(addresses);
        }
    }

    public boolean accept(String address) {
        int addr = Subnet.parseQuattedDot(address);
        if (whitelist.isInList(addr)) {
            return true;
        }
        if (blacklist.isInList(addr)) {
            return false;
        }
        return true;
    }

} // class IPFilter
