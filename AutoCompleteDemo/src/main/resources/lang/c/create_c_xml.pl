#!/usr/local/bin/perl
#
# create_c_xml.pl - Generates the XML API file for RSyntaxTextArea from c.txt
#
# Usage:
#     perl create_c_xml.pl
#
use strict;
use Cwd qw(abs_path);
use File::Basename;


sub fixDesc {

	my $temp = $_[0];

	$temp =~ s/^\s+//;		# Leading whitespace
	$temp =~ s/\n[\n]?$//;	# Final (one or two) newlines
	$temp =~ s!([^>])\n!$1<br>\n!g;	# Newlines (for lines not ending in a tag)

	if ($temp =~ m/[\<\>\&]/) {
		$temp = "<![CDATA[" . $temp . "]]>";
	}
	return $temp;

}


my $this_script = abs_path($0);
my $dir = dirname($this_script);
my $infile = "$dir/c.txt";
my $outfile = "$dir/../c.xml";

my ($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) = localtime(time);
my $datestamp = sprintf("%4d-%02d-%02d %02d:%02d:%02d\n",
					$year+1900,$mon+1,$mday,$hour,$min,$sec);

open(OUT, ">$outfile") || die("Cannot open outfile: $!\n");

# Header information
print OUT <<EOT;
<?xml version=\"1.0\" encoding=\"UTF-8\" ?>
<!DOCTYPE api SYSTEM \"CompletionXml.dtd\">

<!--
   c.xml - API specification for the C Standard Library.
           Used by RSyntaxTextArea to provide code completion.

   Author:         Robert Futrell
   Version:        0.1

   This file was generated from: $infile
   on date: $datestamp
-->
<api language="C">

	<environment paramStartChar="(" paramEndChar=")" paramSeparator=", " terminal=";"/>

	<keywords>


EOT

open(IN, $infile) || die("Cannot open infile: $!\n");

my @elems;
my $item;
my $definedIn;
my @names;
my $line = <IN>;
while (length($line)>0) {

	# Skip header lines and empty lines between items.
	if ($line =~ m/^#+\s+([^ ]+)\s+#+$/) {
		$definedIn = $1;
		$line = <IN>;
		next;
	}
	elsif ($line =~ m/^#.+|^$/) {
		$line = <IN>;
		next;
	}

	if ($line =~ m/^([\w\s]+) (function|constant)/) { # An item to add
		my $name = $1;
		my $returnValDesc;
		push(@names, $name);
		$item = "<keyword name=\"$name\" type=\"$2\"";
		if ($2 eq "function") {
			chomp($line = <IN>);
			if ($line !~ m/^([^\|]+)\|(.*)$/) {
				print("ERROR: Bad format for function return type line: '$line'\n");
				exit(1);
			}
			$item .= " returnType=\"";
			$item .= $1 . "\"";
			$returnValDesc = $2;
		}
		$item .= " definedIn=\"" . $definedIn . "\">\n";
		my $params = "";
		while (chomp($line=<IN>) && ($line =~ m/^[^ ]/)) {
			if ($line =~ m/^(\w+) (\([^\)]+\)\([^\)]+\))\|(.*)$/) { # bsearch - tricky function argument
				$params .= "\t\t<param type=\"" . $1 . "\" name=\"" . $2 . "\"";
				if (length($3)>0) {
					# Try to only put param descs in CDATA if necessary, to keep XML size down.
					my $desc = $3;
					if ($desc =~ m/[<>]/) {
						$desc = fixDesc($desc);
					}
					$params .= ">\n\t\t\t<desc>$desc</desc>\n\t\t</param>\n";
				}
				else {
					$params .= "/>\n";
				}
			}
			elsif ($line =~ m/^(.+)? ([\w_\(\)\*]+(:?\[[\w_]+\])?)\|(.*)$/) {
				$params .= "\t\t<param type=\"" . $1 . "\" name=\"" . $2 . "\"";
				if (length($4)>0) {
					# Try to only put param descs in CDATA if necessary, to keep XML size down.
					my $desc = $4;
					if ($desc =~ m/[<>]/) {
						$desc = fixDesc($desc);
					}
					$params .= ">\n\t\t\t<desc>$desc</desc>\n\t\t</param>\n";
				}
				else {
					$params .= "/>\n";
				}
			}
			elsif ($line =~ m/^\.\.\.\|(.*)$/) {
				$params .= "\t\t<param name=\"...\"";
				if (length($1)>0) {
					# Try to only put param descs in CDATA if necessary, to keep XML size down.
					my $desc = $1;
					if ($desc =~ m/[<>]/) {
						$desc = fixDesc($desc);
					}
					$params .= ">\n\t\t\t<desc>$desc</desc>\n\t\t</param>\n";
				}
				else {
					$params .= "/>\n";
				}
			}
			elsif ($line =~ m/^([\w_])+\|(.*)?$/) {
				$params .= "\t\t<param type=\"" . $1 . "\"";
				if (length($2)>0) {
					# Try to only put param descs in CDATA if necessary, to keep XML size down.
					my $desc = $2;
					if ($desc =~ m/[<>]/) {
						$desc = fixDesc($desc);
					}
					$params .= ">\n\t\t\t<desc>$desc</desc>\n\t\t</param>\n";
				}
				else {
					$params .= "/>\n";
				}
			}
			else {
				print("WARNING: Param line didn't match regex:\n");
				print("\"$line\"\n");
			}
		}
		if (length($params)>0) {
			$item .= "\t<params>\n";
			$item .= $params;
			$item .= "\t</params>\n";
		}
		$item .= "\t<desc>";
		my $desc = "";
		while (defined($line) && ($line =~ m/^$|^ /)) {
			$desc .= substr($line, 1) . "\n";
			chomp($line = <IN>);
		}
		$desc = fixDesc($desc);
		$item .= "$desc</desc>\n";
		if (length($returnValDesc)>0) {
			$item .= "\t<returnValDesc>" . fixDesc($returnValDesc) . "</returnValDesc>\n";
		}
		$item .= "</keyword>";
		#print($item);
		push(@elems, $item);
	}

	else {
		print(STDERR "ERROR: Unexpected line format: \"$line\"\n");
		exit(1);
	}

}

# Get items for the last header.
if (length($item)>0) {
	push(@elems, $item);
}

if (@elems>0) {
	foreach (sort {lc $a cmp lc $b} @elems) {
		my $elem = $_;
		print(OUT "$elem\n");
	}
}

close(IN);

# Print footer of XML definition file
print OUT <<EOT;
	</keywords>

</api>
EOT
