#!/usr/bin/env groovy

def pipeline_id = env.BUILD_ID
println "Current pipeline job build id is '${pipeline_id}'"
def node_label = 'CCI && ansible-2.4'
def mastervertical = MASTERVERTICAL_SCALE_TEST.toString().toUpperCase()

// run mastervert test
stage('mastervertical_scale_test') {
	if (MASTERVERTICAL_SCALE_TEST) {
		currentBuild.result = "SUCCESS"
		node('CCI && US') {
			// get properties file
			if (fileExists("mastervert.properties")) {
				println "Looks like mastervert.properties file already exists, erasing it"
				sh "rm mastervertical.properties"
			}
			// get properties file
			//sh "wget http://file.rdu.redhat.com/~nelluri/pipeline/mastervert.properties"
			sh "wget ${MASTERVERTICAL_PROPERTY_FILE} -O mastervert.properties"
                        sh "cat mastervert.properties"
			def mastervertical_properties = readProperties file: "mastervert.properties"
			def jump_host = mastervertical_properties['JUMP_HOST']
			def user = mastervertical_properties['USER']
			def tooling_inventory_path = mastervertical_properties['TOOLING_INVENTORY']
			def clear_results = mastervertical_properties['CLEAR_RESULTS']
			def move_results = mastervertical_properties['MOVE_RESULTS']
			def containerized = mastervertical_properties['CONTAINERIZED']
			def use_proxy = mastervertical_properties['USE_PROXY']
			def proxy_user = mastervertical_properties['PROXY_USER']
			def proxy_host = mastervertical_properties['PROXY_HOST']
			def projects = mastervertical_properties['PROJECTS']
			def setup_pbench = mastervertical_properties['SETUP_PBENCH']
			def first_run = mastervertical_properties['FIRST_RUN_PROJECTS']
			def second_run = mastervertical_properties['SECOND_RUN_PROJECTS']
			def third_run = mastervertical_properties['THIRD_RUN_PROJECTS']

			// debug info
			println "JUMP_HOST: '${jump_host}'"
			println "USER: '${user}'"
			println "TOOLING_INVENTORY_PATH: '${tooling_inventory_path}'"
			println "CLEAR_RESULTS: '${clear_results}'"
			println "MOVE_RESULTS: '${move_results}'"
			println "CONTAINERIZED: '${containerized}'"
			println "PROXY_USER: '${proxy_user}'"
			println "PROXY_HOST: '${proxy_host}'"
			println "PROJECTS: '${projects}'"

			// Run mastervertical job
			try {
				mastervertical_build = build job: 'mastervert-scale-test',
				parameters: [   [$class: 'LabelParameterValue', name: 'node', label: node_label ],
						[$class: 'StringParameterValue', name: 'JUMP_HOST', value: jump_host ],
						[$class: 'StringParameterValue', name: 'USER', value: user ],
						[$class: 'StringParameterValue', name: 'TOOLING_INVENTORY', value: tooling_inventory_path ],
						[$class: 'BooleanParameterValue', name: 'CLEAR_RESULTS', value: Boolean.valueOf(clear_results) ],
						[$class: 'BooleanParameterValue', name: 'MOVE_RESULTS', value: Boolean.valueOf(move_results) ],
						[$class: 'BooleanParameterValue', name: 'CONTAINERIZED', value: Boolean.valueOf(containerized) ],
						[$class: 'StringParameterValue', name: 'PROXY_USER', value: proxy_user ],
						[$class: 'StringParameterValue', name: 'PROXY_HOST', value: proxy_host ],
						[$class: 'BooleanParameterValue', name: 'USE_PROXY', value: Boolean.valueOf(use_proxy) ],
						[$class: 'BooleanParameterValue', name: 'SETUP_PBENCH', value: Boolean.valueOf(setup_pbench) ],
						[$class: 'StringParameterValue', name: 'FIRST_RUN_PROJECTS', value: first_run ],
						[$class: 'StringParameterValue', name: 'SECOND_RUN_PROJECTS', value: second_run ],
						[$class: 'StringParameterValue', name: 'THIRD_RUN_PROJECTS', value: third_run ]]
			} catch ( Exception e) {
				echo "MASTERVERTICAL-SCALE-TEST Job failed with the following error: "
				echo "${e.getMessage()}"
				echo "Sending an email"
				mail(
      					to: 'nelluri@redhat.com',
      					subject: 'Mastervertical-scale-test job failed',
      					body: """\
						Encoutered an error while running the mastervertical-scale-test job: ${e.getMessage()}\n\n
						Jenkins job: ${env.BUILD_URL}
				""")
				currentBuild.result = "FAILURE"
				sh "exit 1"
			}
			println "MASTERVERTICAL-SCALE-TEST build ${mastervertical_build.getNumber()} completed successfully"
		}
	}
}
