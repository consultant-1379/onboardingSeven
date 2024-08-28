package TestTools;

use strict;
use warnings;
use Exporter;
use Cwd 'realpath';
use File::Basename;
use File::Spec;

our @ISA= qw(Exporter);

# these CAN be exported.
our @EXPORT_OK = qw(startup);

# these are exported by default.
our @EXPORT = qw(startup);

our $isCygwin = 1;
our $install_directory;
our $app;
our $product;
our $debug = 0;
our $suspend = "n";

sub startup {
    our $product = shift;
    our $app = shift;
    get_installation_directory();

    my $java_binary = get_java_binary();
    my @jarnames = get_jarnames();
    my $classpath = get_classpath(@jarnames);
        
    my @cmd = ($java_binary, "-cp", $classpath);
    
    my @args = process_args();
    if ($debug) {
        push(@cmd, "-agentlib:jdwp=transport=dt_socket,server=y,suspend=" . $suspend . ",address=" . $debug);
    }
    push(@cmd, $app);

#    print join(" ", map qq("$_"), @cmd, @args) . "\n";

    exec(@cmd, @args);
}

sub process_args {
    my @args;
    my $pathpattern = "^/";
    my $isdebugarg = 0;

    foreach my $arg (@ARGV) {
	if ($arg eq "--debug") {
            $isdebugarg = 1;
        }
	elsif ($arg eq "--suspend") {
            $suspend = "y";
        }
        elsif ($isdebugarg) {
            if (!($arg =~ m/^[0-9]*$/) || ($arg < 0) || ($arg > 65535)) {
                print "$arg is not a valid debug port!\n";
                exit 1;
            }
	    $debug = $arg;
            $isdebugarg = 0;
        }
        elsif ($isCygwin && ($arg =~ m/$pathpattern/)) {
	    my $path = `cygpath -wp -- "$arg"`;
	    chomp($path);
            push(@args, $path);
        }
        else {
            push(@args, $arg);
        }
    }
    return @args;
}
		
sub get_jarnames {
    open (FILE, $install_directory . "/etc/" . $product . ".classpath");
    my @jarnames = (<FILE>);
    close(FILE);
    @jarnames = split(':', $jarnames[0]);
    return @jarnames;
}
sub check_installation_directory {
    return (-e $install_directory . "/etc/" . $product . ".classpath");
}

sub get_installation_directory {
    our $install_directory = dirname(dirname(realpath($0)));
    if (check_installation_directory()) {
        return;
    }
    $install_directory = dirname(realpath($0)) . "/tools/" . $product;
    if (check_installation_directory()) {
        return;
    }
    print "\nERROR: Unable to locate home folder\n";
    exit 0;
}

sub get_java_binary {
    my $pattern6 = "1.6";
    my $pattern7 = "1.7";
    my $pattern8 = "1.8";
    my $java_binary_installed = qx(where java);
    my @java_binarys = split /^/, $java_binary_installed;
    
    foreach (@java_binarys)	{
    	my $java_binary = substr $_, 0, (length $_) - 2; 
    	if (index($java_binary, $pattern8) != -1)	{
    		return $java_binary;
    	} elsif (index($java_binary, $pattern7) != -1)	{
    		return $java_binary;
    	} elsif (index($java_binary, $pattern6) != -1)	{
    		return $java_binary;
    	}
    }

    print "\nERROR: Unable to locate valid Java version (1.6 or later required)\n";
    exit 0;
}

sub get_classpath {
    my @jarnames = @_;
    my @jarfiles;

    foreach my $jarname (@jarnames) {
        push(@jarfiles, File::Spec->catdir($install_directory, $jarname));
    }

    my $classpath = join(";", @jarfiles);
    $classpath =~ s{/}{\\}g;
    $classpath =~ s/;lib/;CSLDecoder\\tools\\ltng\\lib\\/g;
    $classpath = substr $classpath, 1;
    return $classpath;
}

1;
