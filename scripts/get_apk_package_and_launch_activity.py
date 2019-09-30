#!/usr/bin/python3
import argparse
from subprocess import run, PIPE


def main():
    parser = argparse.ArgumentParser(description="Take the given path of an apk and "
                                                 "grab the package name along with the launchable activity name")
    parser.add_argument('apk_path', help="The path of the apk file")

    args = parser.parse_args()

    apk_info = get_apk_info(args.apk_path)
    package_name = grep(apk_info, "package")
    launch_activity = grep(apk_info, "launchable-activity")

    print(package_name)
    print(launch_activity)
    print()


def get_apk_info(apk_path):
    appt_tool = "~/Android/Sdk/build-tools/29.0.0/aapt"
    appt_args = [appt_tool, 'd', 'badging', apk_path]

    appt_info = run(" ".join(appt_args), stdout=PIPE, shell=True, universal_newlines=True)

    return appt_info.stdout


def grep(text_to_be_searched, search_pattern):
    result = run("echo \"{}\" | grep -i {}".format(text_to_be_searched, search_pattern),
                 stdout=PIPE, shell=True, universal_newlines=True)
    return result.stdout


if __name__ == "__main__":
    main()
